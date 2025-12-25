package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CustomerNoteDTO {
    private Integer noteId;
    private Integer customerId;
    private String noteText;
    private String noteType;
    private String createdBy;
    private LocalDateTime createdAt;
}