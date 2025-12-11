package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "LeaveRequests")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    private Integer staffId;

    @ManyToOne
    @JoinColumn(name = "StaffId", insertable = false, updatable = false)
    private UserAccount staff;

    private Integer createdByStaffId;
    private LocalDate fromDate;
    private LocalDate toDate;
    @Column(name = "Reason", columnDefinition = "NVARCHAR(500)") // Thêm dòng này
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String leaveType; // ANNUAL, UNPAID, SICK
    @Column(name = "AdminNote", columnDefinition = "NVARCHAR(500)") // Thêm dòng này
    private String adminNote;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters & Setters
    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }
    public UserAccount getStaff() { return staff; }
    public Integer getCreatedByStaffId() { return createdByStaffId; }
    public void setCreatedByStaffId(Integer createdByStaffId) { this.createdByStaffId = createdByStaffId; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
}