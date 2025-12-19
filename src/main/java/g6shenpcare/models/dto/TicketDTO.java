package g6shenpcare.models.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TicketDTO {
    private Long id;
    private String subject;
    private String category;
    private String priority;
    private String status;
    private String assignedTo;

}
