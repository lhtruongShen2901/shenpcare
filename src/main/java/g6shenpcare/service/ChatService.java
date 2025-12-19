package g6shenpcare.service;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.dto.ChatSessionSummaryDTO;
import g6shenpcare.models.dto.MessageDTO;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.entity.Message;
import g6shenpcare.models.enums.ESessionStatus;
import g6shenpcare.models.mapper.MessageMapper;
import g6shenpcare.repository.ChatSessionRepository;
import g6shenpcare.repository.MessageRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatSessionRepository sessionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageMapper messageMapper;



    @Transactional
    public Optional<ChatSession> getSessionWithMessages(Integer staffId, Integer customerId) {
        Optional<ChatSession> sessionOpt = sessionRepository
                .findBySupportStaff_UserIdAndCustomer_UserIdAndStatus(Long.valueOf(staffId),Long.valueOf(customerId),ESessionStatus.ACTIVE);

        sessionOpt.ifPresent(session -> session.getMessages().size());

        return sessionOpt;
    }
    @Transactional(readOnly = true)
    public Optional<ChatSession> findActiveChatSession(int customerId) {
        UserAccount customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return sessionRepository.findActiveSessionByCustomer(customer);
    }


    @Transactional
    public ChatSession startChatSession(int customerId) {
        UserAccount customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Optional<ChatSession> existingSession = sessionRepository.findActiveSessionByCustomer(customer);
        if (existingSession.isPresent()) {
            return existingSession.get();
        }

        ChatSession session = new ChatSession();
        session.setCustomer(customer);
        session.setStatus(ESessionStatus.WAITING);
        session.setStartedAt(LocalDateTime.now());

        ChatSession savedSession = sessionRepository.save(session);

        // Thông báo cho support staff có khách hàng đang chờ
        messagingTemplate.convertAndSend("/topic/waiting-customers", savedSession);

        return savedSession;
    }

    @Transactional
        public ChatSession assignStaffToSession(Long sessionId, int staffId) {

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        UserAccount staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        if (!Objects.equals(staff.getRole(), "SUPPORT")
                && !Objects.equals(staff.getRole(), "ADMIN")) {
            throw new RuntimeException("User is not a support staff");
        }


        session.setSupportStaff(staff);
        session.setStatus(ESessionStatus.ACTIVE);

        ChatSession savedSession = sessionRepository.save(session);

        // 2️ Gửi status cho customer (header đổi realtime)
        messagingTemplate.convertAndSend(
                STR."/topic/session/\{sessionId}/status",
                Map.of(
                        "status", "ACTIVE",
                        "staffName", staff.getFullName()
                )
        );

        Message systemMsg = new Message();
        systemMsg.setSession(savedSession);
        systemMsg.setSender(staff);
        systemMsg.setMessageText(
                STR."Nhân viên \{staff.getFullName()} đã tham gia hỗ trợ"
        );
        systemMsg.setSentAt(LocalDateTime.now());
        systemMsg.setIsRead(true);

        Message savedSystemMsg = messageRepository.save(systemMsg);

        messagingTemplate.convertAndSend(
                STR."/topic/session/\{sessionId}/messages",
                messageMapper.toDto(savedSystemMsg)
        );

        return savedSession;
    }

    public Map<String, Object> getSessionStatus(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() == ESessionStatus.ACTIVE && session.getSupportStaff() != null) {
            return Map.of(
                    "status", "ACTIVE",
                    "staffName", session.getSupportStaff().getFullName()
            );
        }

        return Map.of("status", "WAITING");
    }



    private String shorten(String text) {
        return text.length() > 40 ? STR."\{text.substring(0, 40)}…" : text;
    }


    public List<ChatSessionSummaryDTO> getActiveSessionSummaries(int staffId) {

        UserAccount staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        List<ChatSession> sessions =
                sessionRepository.findBySupportStaffAndStatus(staff, ESessionStatus.ACTIVE);

        return sessions.stream().map(session -> {

            Message lastMessage =
                    messageRepository.findTopBySessionOrderBySentAtDesc(session);

            long unreadCount =
                    messageRepository.countUnreadMessages(session, staffId);

            String preview;

            if (lastMessage == null) {
                preview = "Chưa có tin nhắn";
            } else if (lastMessage.getSender().getUserId().equals(staffId)) {
                preview = STR."Bạn: \{ shorten(lastMessage.getMessageText())}";
            } else {
                preview = STR."\{lastMessage.getSender().getUsername()}: \{ shorten(lastMessage.getMessageText())}";
            }

            return new ChatSessionSummaryDTO(
                    session.getId(),
                    session.getCustomer(),
                    session.getStartedAt(),
                    preview,
                    unreadCount
            );
        }).toList();
    }



    @Transactional
    public Message sendMessage(Long sessionId, int senderId, String messageText) {

        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        UserAccount sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Message message = new Message();
        message.setSession(session);
        message.setSender(sender);
        message.setMessageText(messageText);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);

        Message saved = messageRepository.save(message);
        MessageDTO dto = messageMapper.toDto(saved);

        messagingTemplate.convertAndSend(
                STR."/topic/session/\{sessionId}/messages",
                dto
        );

        if (session.getSupportStaff() != null) {
            Integer staffId = session.getSupportStaff().getUserId();
            messagingTemplate.convertAndSend(
                    STR."/topic/staff/\{staffId}/session-messages",
                    dto
            );
        }

        return saved;
    }


    public List<Message> getSessionMessages(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return messageRepository.findBySessionOrderBySentAtAsc(session);
    }

    @Transactional
    public void markMessagesAsRead(Long sessionId, Integer userId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        List<Message> unreadMessages = messageRepository.findUnreadMessagesBySession(session);
        unreadMessages.stream()
                .filter(m -> !m.getSender().getUserId().equals(userId))
                .forEach(m -> m.setIsRead(true));

        messageRepository.saveAll(unreadMessages);
    }

    @Transactional
    public ChatSession closeSession(Long sessionId) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(ESessionStatus.CLOSED);
        session.setEndedAt(LocalDateTime.now());

        return sessionRepository.save(session);
    }

    public List<ChatSession> getWaitingSessions() {
        return sessionRepository.findByStatus(ESessionStatus.WAITING);
    }

    public List<ChatSession> getActiveSessionsForStaff(int staffId) {
        UserAccount staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        return sessionRepository.findBySupportStaffAndStatus(staff, ESessionStatus.ACTIVE);
    }

    public UserAccount findAvailableStaff() {
        List<UserAccount> staffList = userRepository.findByRoleAndActive("SUPPORT", true);

        // Tìm staff có ít session active nhất
        UserAccount availableStaff = null;
        Long minActiveSessions = Long.MAX_VALUE;

        for (UserAccount staff : staffList) {
            Long activeSessions = sessionRepository.countActiveSessionsByStaff(staff);
            if (activeSessions < minActiveSessions) {
                minActiveSessions = activeSessions;
                availableStaff = staff;
            }
        }

        return availableStaff;
    }
}

