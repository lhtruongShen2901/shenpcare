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
                    "/topic/user/" + ticket.getAssignedTo().getUserId() + "/tickets",
                    ticket
            );
        }
    }

    public void notifyNewMessage(Long sessionId, Long userId) {
        messagingTemplate.convertAndSend(
                "/topic/user/" + userId + "/notifications",
                "New message in session " + sessionId

        );
    }
}