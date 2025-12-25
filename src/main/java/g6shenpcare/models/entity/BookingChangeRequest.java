package g6shenpcare.models.entity;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BookingChangeRequests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingChangeRequestId")
    private Integer bookingChangeRequestId;

    @ManyToOne
    @JoinColumn(name = "BookingId", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private CustomerProfile customer;

    @Column(name = "RequestType", nullable = false, length = 20)
    private String requestType;

    @Column(name = "RequestedStartTime")
    private LocalDateTime requestedStartTime;

    @Column(name = "Reason", length = 500)
    private String reason;

    @Column(name = "Status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "ProcessedByStaffId")
    private StaffProfile processedByStaff;

    @Column(name = "ProcessedAt")
    private LocalDateTime processedAt;
}
