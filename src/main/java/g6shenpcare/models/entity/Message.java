package g6shenpcare.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import g6shenpcare.entity.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private ChatSession session;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserAccount sender;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String messageText;

    private LocalDateTime sentAt = LocalDateTime.now();
    private Boolean isRead = false;
}
