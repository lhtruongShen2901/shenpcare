package g6shenpcare.models.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class QuickBookingRequest {
    private Long sessionId;
    private Integer customerId;
    private Integer petId;
    private Integer serviceId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private Integer staffId;
    private String notes;
}
