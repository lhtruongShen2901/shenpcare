package g6shenpcare.models.entity;

import g6shenpcare.entity.Services;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "BookingSlotConfig")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSlotConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SlotConfigId")
    private Integer slotConfigId;

    @ManyToOne
    @JoinColumn(name = "ServiceId")
    private Services service;

    @ManyToOne
    @JoinColumn(name = "StaffId")
    private StaffProfile staff;

    @Column(name = "DayOfWeek", nullable = false)
    private Short dayOfWeek;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "MaxBookings", nullable = false)
    private Integer maxBookings;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
