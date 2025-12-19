package g6shenpcare.models.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private String messageText;
    private LocalDateTime sentAt;
    private SessionDTO session;
    private UserDTO sender;
}
