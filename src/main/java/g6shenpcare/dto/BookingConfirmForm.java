package g6shenpcare.dto;

import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public class BookingConfirmForm {
    private Integer bookingId;
    private Integer assignedStaffId; 
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime confirmTime;   

    // --- Getters & Setters ---
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    
    public Integer getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(Integer assignedStaffId) { this.assignedStaffId = assignedStaffId; }
    
    public LocalTime getConfirmTime() { return confirmTime; }
    public void setConfirmTime(LocalTime confirmTime) { this.confirmTime = confirmTime; }

    // [THÊM MỚI] Hàm này giúp BookingService gọi được getStartTime() mà không bị lỗi
    public LocalTime getStartTime() {
        return this.confirmTime; 
    }
}