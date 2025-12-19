    package g6shenpcare.service;

    import g6shenpcare.entity.UserAccount;
    import g6shenpcare.models.dto.TicketDTO;
    import g6shenpcare.models.entity.ChatSession;
    import g6shenpcare.models.entity.Ticket;
    import g6shenpcare.models.enums.EPriority;
    import g6shenpcare.models.enums.ETicketCategory;
    import g6shenpcare.models.enums.ETicketStatus;
    import g6shenpcare.repository.ChatSessionRepository;
    import g6shenpcare.repository.TicketRepository;
    import g6shenpcare.repository.UserAccountRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;

    @Service
    public class TicketService {

        @Autowired
        private TicketRepository ticketRepository;

        @Autowired
        private UserAccountRepository userRepository;

        @Autowired
        private ChatSessionRepository sessionRepository;

        @Autowired
        private NotificationService notificationService;

        @Autowired
        private SimpMessagingTemplate messagingTemplate;


        @Transactional
        public Ticket createTicket(Long sessionId, int createdById, ETicketCategory category,
                                   EPriority priority, String subject, String description, Integer assignToId) {

            UserAccount createdBy = userRepository.findById(createdById)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            ChatSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found"));

            Ticket ticket = new Ticket();
            ticket.setSession(session);
            ticket.setCustomer(session.getCustomer());
            ticket.setCreatedBy(createdBy);
            ticket.setCategory(category);
            ticket.setPriority(priority);
            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setStatus(ETicketStatus.OPEN);
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            UserAccount assignedToUser = null;
            if (assignToId != null) {
                assignedToUser = userRepository.findById(assignToId)
                        .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                ticket.setAssignedTo(assignedToUser);
                notificationService.notifyTicketAssignment(ticket);
            }

            Ticket savedTicket = ticketRepository.save(ticket);


            messagingTemplate.convertAndSendToUser(
                    createdBy.getUsername(),
                    "/queue/tickets/opened",
                    savedTicket
            );

            if (assignedToUser != null && !assignedToUser.getUserId().equals(createdBy.getUserId())) {
                messagingTemplate.convertAndSendToUser(
                        assignedToUser.getUsername(),
                        "/queue/tickets/opened",
                        savedTicket
                );
            }

            return savedTicket;
        }



        public Ticket getTicketById(Long ticketId) {
            return ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException(STR."Ticket not found with id: \{ticketId}"));
        }

        public Ticket updateNote(Ticket ticket) {
            return ticketRepository.save(ticket);
        }


        public List<Ticket> getAllTickets() {
            return ticketRepository.findAll();
        }

        @Transactional
        public Ticket updateTicketStatus(Long ticketId, ETicketStatus newStatus) {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            ticket.setStatus(newStatus);
            ticket.setUpdatedAt(LocalDateTime.now());

            if (newStatus == ETicketStatus.RESOLVED || newStatus == ETicketStatus.CLOSED) {
                ticket.setResolvedAt(LocalDateTime.now());
            }

            return ticketRepository.save(ticket);
        }


        @Transactional
        public Ticket assignTicket(Long ticketId, Integer assignToId) {
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            UserAccount previousAssignee = ticket.getAssignedTo();
            UserAccount newAssignee = null;
            UserAccount creator = ticket.getCreatedBy();

            if (assignToId != null) {
                newAssignee = userRepository.findById(assignToId)
                        .orElseThrow(() -> new RuntimeException("Assigned user not found"));
                ticket.setAssignedTo(newAssignee);
            } else {
                ticket.setAssignedTo(null); // unassign
            }

            ticket.setUpdatedAt(LocalDateTime.now());
            Ticket updatedTicket = ticketRepository.save(ticket);


            if (newAssignee != null) {
                messagingTemplate.convertAndSendToUser(
                        newAssignee.getUsername(),
                        "/queue/tickets/opened",
                        updatedTicket
                );

                if (!newAssignee.getUserId().equals(creator.getUserId())) {
                    messagingTemplate.convertAndSendToUser(
                            creator.getUsername(),
                            "/queue/tickets/opened",
                            updatedTicket
                    );
                }
            }

            if (previousAssignee != null) {
                boolean shouldRemove = false;

                if (newAssignee == null) {
                    shouldRemove = true;
                } else if (!previousAssignee.getUserId().equals(newAssignee.getUserId())) {
                    shouldRemove = true;
                }

                if (shouldRemove && !previousAssignee.getUserId().equals(creator.getUserId())) {
                    messagingTemplate.convertAndSendToUser(
                            previousAssignee.getUsername(),
                            "/queue/tickets/removed",
                            updatedTicket.getId()
                    );
                }
            }

            return updatedTicket;
        }


        public List<Ticket> getOpenTickets() {
            return ticketRepository.findOpenTickets();
        }

        public List<Ticket> getOpenTicketsForStaff(Integer staffId) {
            return ticketRepository.findOpenTicketsForStaff(staffId);
        }

        public List<Ticket> getTicketsByCustomer(Integer customerId) {
            UserAccount customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            return ticketRepository.findByCustomer(customer);
        }

        public List<Ticket> getTicketsAssignedToUser(Integer userId) {
            UserAccount user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ticketRepository.findByAssignedTo(user);
        }


        public List<Ticket> getTicketsByStatus(ETicketStatus status) {
            return ticketRepository.findByStatus(status);
        }


        public List<Ticket> getTicketsByPriority(EPriority priority) {
            return ticketRepository.findByPriority(priority);
        }

        @Transactional
        public Ticket closeTicket(Long ticketId) {
            return updateTicketStatus(ticketId, ETicketStatus.CLOSED);
        }

        @Transactional
        public Ticket resolveTicket(Long ticketId) {
            return updateTicketStatus(ticketId, ETicketStatus.RESOLVED);
        }

        public List<Ticket> getMyOpenTickets(Integer staffId) {
            return ticketRepository.findOpenTicketsCreatedBy(staffId);
        }

        public List<Ticket> getAssignedOpenTickets(Integer staffId) {
            return ticketRepository.findOpenTicketsAssignedTo(staffId);
        }

    }

