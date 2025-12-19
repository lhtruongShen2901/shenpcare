package g6shenpcare.service;

import g6shenpcare.models.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyTicketAssignment(Ticket ticket) {
        if (ticket.getAssignedTo() != null) {
            messagingTemplate.convertAndSend(
                    STR."/topic/user/\{ticket.getAssignedTo().getUserId()}/tickets",
                    ticket
            );
        }
    }

    public void notifyNewMessage(Long sessionId, Long userId) {
        messagingTemplate.convertAndSend(
                STR."/topic/user/\{userId}/notifications",
                STR."New message in session \{sessionId}"
        );
    }
}