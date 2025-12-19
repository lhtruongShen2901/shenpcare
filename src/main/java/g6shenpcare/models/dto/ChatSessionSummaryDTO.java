package g6shenpcare.models.dto;

import g6shenpcare.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ChatSessionSummaryDTO {
    private Long id;
    private UserAccount customer;
    private LocalDateTime startedAt;

    private String lastMessagePreview;
    private long unreadCount;
}

