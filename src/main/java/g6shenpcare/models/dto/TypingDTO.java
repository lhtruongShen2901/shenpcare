package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TypingDTO {
    private Long sessionId;
    private Integer userId;
}
