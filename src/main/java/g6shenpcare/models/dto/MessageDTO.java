package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private Long id;
    private String messageText;
    private LocalDateTime sentAt;
    private SessionDTO session;
    private UserDTO sender;
}

