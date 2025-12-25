package g6shenpcare.models.entity;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.UserAccount;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "CustomerNotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NoteId")
    private Integer noteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", nullable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CreatedByUserId")
    private UserAccount createdBy;

    @Column(name = "NoteText", columnDefinition = "NVARCHAR(1000)", nullable = false)
    private String noteText;

    @Column(name = "NoteType", length = 20)
    private String noteType; // INFO, PREFERENCE, WARNING

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "RelatedSessionId")
    private Integer relatedSessionId;
}
