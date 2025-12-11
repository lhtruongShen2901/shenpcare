package g6shenpcare.dto;

import java.time.LocalDate;

public class LeaveRequestDetailDTO {
    
    // Thông tin cơ bản từ Entity LeaveRequest
    private Integer requestId;
    private String staffName;
    private String staffRole;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String reason;
    private String status;    // PENDING, APPROVED, REJECTED
    private String leaveType; // ANNUAL, UNPAID...
    
    // Thông tin tính toán thêm (Calculated Fields)
    private int daysRequested;      // Tổng số ngày xin nghỉ trong đơn này
    private int currentQuota;       // Quỹ phép hiện tại của nhân viên
    private int excessDays;         // Số ngày vượt mức quỹ (dùng để cảnh báo Admin)

    // Constructor mặc định
    public LeaveRequestDetailDTO() {
    }

    // --- Getters & Setters ---

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffRole() {
        return staffRole;
    }

    public void setStaffRole(String staffRole) {
        this.staffRole = staffRole;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public int getDaysRequested() {
        return daysRequested;
    }

    public void setDaysRequested(int daysRequested) {
        this.daysRequested = daysRequested;
    }

    public int getCurrentQuota() {
        return currentQuota;
    }

    public void setCurrentQuota(int currentQuota) {
        this.currentQuota = currentQuota;
    }

    public int getExcessDays() {
        return excessDays;
    }

    public void setExcessDays(int excessDays) {
        this.excessDays = excessDays;
    }
}