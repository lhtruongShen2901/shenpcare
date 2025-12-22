package g6shenpcare.dto;

import java.time.LocalDate;
import java.util.List;
import jakarta.validation.constraints.NotNull;

public class BookingRequestDTO {

    // --- Khách hàng ---
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;

    // --- Dịch vụ & Thời gian ---
    @NotNull(message = "Vui lòng chọn dịch vụ")
    private Integer serviceId;

    @NotNull(message = "Vui lòng chọn ngày")
    private LocalDate bookingDate;

    private String timeSlot;

    // --- Thú Cưng (Logic: Tùy chọn & Chọn nhiều) ---
    
    // Danh sách ID các bé đã có hồ sơ (Dùng Checkbox chọn nhiều)
    private List<Long> selectedPetIds;

    // ID đơn lẻ (dùng nội bộ khi tách đơn, hoặc nếu form gửi lên 1 bé)
    private Long petId; 

    // Các trường nhập tay (Cho bé mới - Không bắt buộc)
    private String petName;
    private String petSpecies;
    private String petBreed;
    private Integer petAge;
    private Double petWeight;

    // --- Khác ---
    private String notes;
    private boolean isUrgent;

    // --- Getters & Setters ---
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public List<Long> getSelectedPetIds() { return selectedPetIds; }
    public void setSelectedPetIds(List<Long> selectedPetIds) { this.selectedPetIds = selectedPetIds; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }

    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }

    public Integer getPetAge() { return petAge; }
    public void setPetAge(Integer petAge) { this.petAge = petAge; }

    public Double getPetWeight() { return petWeight; }
    public void setPetWeight(Double petWeight) { this.petWeight = petWeight; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(boolean urgent) { isUrgent = urgent; }
}