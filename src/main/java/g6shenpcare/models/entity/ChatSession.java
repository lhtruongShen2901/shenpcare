package g6shenpcare.models.entity;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.enums.ESessionStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private UserAccount customer;

    @ManyToOne
    @JoinColumn(name = "support_staff_id")
    private UserAccount supportStaff;

    @Enumerated(EnumType.STRING)
    private ESessionStatus status = ESessionStatus.WAITING;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime endedAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Message> messages;
}

