package g6shenpcare.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookingMonitorDTO {
    private Integer bookingId;
    
    // Khách & Pet
    private String customerName;
    private String customerPhone;
    private String petName;
    private String petSpecies;
    private String petBreed;
    
    // Dịch vụ
    private String serviceType; // Khám/Spa
    private String serviceName;
    private String serviceNote; // Ghi chú của khách
    private boolean isUrgent;   // Cờ khẩn cấp
    
    // Thời gian & Trạng thái
    private LocalDate bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;      // PENDING, CONFIRMED...
    
    private String staffName;

    public BookingMonitorDTO() {}

    // Helper: Hiển thị giờ + Nhãn khẩn cấp (Dùng cho giao diện Admin)
    public String getTimeSlotDisplay() {
        String timeStr = "Chưa chốt giờ";
        if (startTime != null) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
            timeStr = startTime.format(dtf);
            if (endTime != null) {
                timeStr += " - " + endTime.format(dtf);
            }
        }
        // Nếu khẩn cấp thì thêm nhãn báo động
        if (isUrgent) {
            return timeStr + " (🔥 KHẨN CẤP)";
        }
        return timeStr;
    }

    // --- GIỮ NGUYÊN GETTER/SETTER CŨ CỦA BẠN ---
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }
    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }
    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getServiceNote() { return serviceNote; }
    public void setServiceNote(String serviceNote) { this.serviceNote = serviceNote; }
    public boolean isUrgent() { return isUrgent; }
    public void setUrgent(boolean urgent) { isUrgent = urgent; }
    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
}