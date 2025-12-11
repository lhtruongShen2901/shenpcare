package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "StaffWorkingSchedule")
public class StaffWorkingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ScheduleId")
    private Integer scheduleId;

    @Column(name = "StaffId")
    private Integer staffId;

    // Join để lấy tên nhân viên (Optional - dùng khi hiển thị)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StaffId", insertable = false, updatable = false)
    private UserAccount staff;

    @Column(name = "WorkDate")
    private LocalDate workDate; // Lưu ngày cụ thể: 2025-12-04

    @Column(name = "DayOfWeek")
    private Integer dayOfWeek; // Vẫn giữ để filter nhanh nếu cần (1=Mo
    
    @Column(name = "StartTime", nullable = false)
    private LocalTime startTime;

    @Column(name = "EndTime", nullable = false)
    private LocalTime endTime;

    @Column(name = "MaxDailyBookings")
    private Integer maxDailyBookings; // Có thể null nếu quản lý theo thời gian

    @Column(name = "IsActive")
    private boolean active = true;

    public StaffWorkingSchedule() {
    }

    // --- Getter & Setter ---
    // --- Getter & Setter ---
    // (Bổ sung getter/setter cho workDate, các trường khác giữ nguyên)
    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public UserAccount getStaff() {
        return staff;
    }

    public void setStaff(UserAccount staff) {
        this.staff = staff;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getMaxDailyBookings() {
        return maxDailyBookings;
    }

    public void setMaxDailyBookings(Integer maxDailyBookings) {
        this.maxDailyBookings = maxDailyBookings;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
