package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingId")
    private Integer bookingId;

    @Column(name = "CustomerId", insertable = false, updatable = false)
    private Integer customerId;

    @Column(name = "PetId", insertable = false, updatable = false)
    private Integer petId;

    @Column(name = "ServiceId", insertable = false, updatable = false)
    private Integer serviceId;

    @Column(name = "AssignedStaffId", insertable = false, updatable = false)
    private Integer assignedStaffId;

    // --- [QUAN TRỌNG] THÊM MỐI QUAN HỆ (RELATIONSHIPS) ---
    // Để BookingService có thể gọi b.getCustomer().getFullName()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId")
    private CustomerProfile customer; // Hoặc UserAccount tùy DB bạn, ở đây dùng CustomerProfile

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PetId")
    private Pets pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceId")
    private Services service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedStaffId")
    private UserAccount staff; // Nhân viên là UserAccount

    // --- CÁC TRƯỜNG THÔNG TIN KHÁC ---
    @Column(name = "BookingDate", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;

    @Column(name = "PaymentStatus", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "TotalAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "PENDING_CONFIRMATION";
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "UNPAID";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
