package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingDTO {
    private Integer bookingId;
    private Integer customerId;
    private String customerName;
    private Integer petId;
    private String petName;
    private Integer serviceId;
    private String serviceName;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private Integer assignedStaffId;
    private String assignedStaffName;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;
}