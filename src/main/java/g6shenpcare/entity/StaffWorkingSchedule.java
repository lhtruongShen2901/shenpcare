package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "StaffWorkingSchedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffWorkingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleId")
    private Integer scheduleId;

    @Column(name = "StaffId")
    private Integer staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StaffId", insertable = false, updatable = false)
    private UserAccount staff;

    @Column(name = "WorkDate")
    private LocalDate workDate;

    @Column(name = "DayOfWeek")
    private Integer dayOfWeek;

    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "MaxDailyBookings")
    private Integer maxDailyBookings;

    @Column(name = "IsActive")
    private boolean active = true;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

