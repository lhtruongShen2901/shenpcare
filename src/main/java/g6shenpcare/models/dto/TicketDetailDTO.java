package g6shenpcare.models.dto;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.models.entity.Message;
import g6shenpcare.models.entity.Ticket;
import lombok.Data;

import java.util.List;

@Data
public class TicketDetailDTO {
    private Ticket ticket;
    private CustomerProfile customer;
    private List<Message> messages;
}
