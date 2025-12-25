package g6shenpcare.models.entity;


import g6shenpcare.entity.Booking;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "GroomingBoardingProgress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroomingBoardingProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProgressId")
    private Integer progressId;

    @ManyToOne
    @JoinColumn(name = "BookingId", nullable = false)
    private Booking booking;

    @Column(name = "Status", nullable = false, length = 50)
    private String status;
    // Nếu có enum:
    // @Enumerated(EnumType.STRING)

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "UpdatedByStaffId")
    private StaffProfile updatedBy;

    @Column(name = "Notes", length = 500)
    private String notes;
}
