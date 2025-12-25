package g6shenpcare.models.dto;

import lombok.Data;

@Data
public class CustomerNoteRequest {
    private Integer customerId;
    private Integer sessionId;
    private String noteText;
    private String noteType; // PREFERENCE, WARNING, INFO
}