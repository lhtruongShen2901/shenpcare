package g6shenpcare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingId")
    private Integer bookingId;

    // --- CỘT ID (Dùng để ghi dữ liệu vào DB) ---
    @Column(name = "CustomerId")
    private Integer customerId;

    @Column(name = "PetId")
    private Integer petId;

    @Column(name = "ServiceId")
    private Integer serviceId;

    @Column(name = "AssignedStaffId")
    private Integer assignedStaffId;

    // --- RELATIONSHIPS (Dùng để đọc dữ liệu hiển thị lên Web) ---
    // insertable=false, updatable=false: Nghĩa là chỉ dùng để JOIN bảng, không UPDATE qua biến này
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", insertable = false, updatable = false)
    private CustomerProfile customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PetId", insertable = false, updatable = false)
    private Pets pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceId", insertable = false, updatable = false)
    private Services service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AssignedStaffId", insertable = false, updatable = false)
    private UserAccount staff;

    // --- THÔNG TIN CHI TIẾT ---
    @Column(name = "BookingDate", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "StartTime") // Có thể null lúc mới đặt
    private LocalDateTime startTime;

    @Column(name = "EndTime")
    private LocalDateTime endTime;

    @Column(name = "Status", nullable = false, length = 20)
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED

    @Column(name = "PaymentStatus", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "TotalAmount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "Notes", length = 500)
    private String notes;
    
    @Column(name = "IsUrgent")
    private Boolean isUrgent = false;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    public Booking() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "PENDING";
        if (this.paymentStatus == null) this.paymentStatus = "UNPAID";
        if (this.isUrgent == null) this.isUrgent = false;
        if (this.totalAmount == null) this.totalAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ---
    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public Integer getAssignedStaffId() { return assignedStaffId; }
    public void setAssignedStaffId(Integer assignedStaffId) { this.assignedStaffId = assignedStaffId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean urgent) { isUrgent = urgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // --- RELATIONSHIP HELPERS ---
    // Các hàm này giúp việc set Object trong Controller tự động điền ID vào DB
    public CustomerProfile getCustomer() { return customer; }
    public void setCustomer(CustomerProfile customer) {
        this.customer = customer;
        if (customer != null) this.customerId = customer.getCustomerId();
    }

    public Pets getPet() { return pet; }
    public void setPet(Pets pet) {
        this.pet = pet;
        if (pet != null) this.petId = pet.getPetId();
    }

    public Services getService() { return service; }
    public void setService(Services service) {
        this.service = service;
        if (service != null) this.serviceId = service.getServiceId();
    }
    
    public UserAccount getStaff() { return staff; }
    public void setStaff(UserAccount staff) {
        this.staff = staff;
        if(staff != null) this.assignedStaffId = staff.getUserId();
    }
}