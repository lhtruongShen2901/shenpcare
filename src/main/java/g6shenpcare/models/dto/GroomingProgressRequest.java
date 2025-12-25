package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroomingProgressRequest {
    private String[] checklist;
    private String notes;
    private String action; // "update_progress" hoặc "complete"
}