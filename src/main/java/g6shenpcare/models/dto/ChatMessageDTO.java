package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageDTO {
    private Long sessionId;
    private Integer senderId;
    private String messageText;
}

