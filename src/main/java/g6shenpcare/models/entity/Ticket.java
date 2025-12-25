package g6shenpcare.models.entity;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.enums.EPriority;
import g6shenpcare.models.enums.ETicketCategory;
import g6shenpcare.models.enums.ETicketStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SupportTickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private UserAccount customer;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserAccount createdBy;

    @Enumerated(EnumType.STRING)
    private ETicketCategory category;

    @Enumerated(EnumType.STRING)
    private EPriority priority;

    private String subject;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    private ETicketStatus status = ETicketStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private UserAccount assignedTo;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String note;


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;
}