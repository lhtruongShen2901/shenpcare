package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class BookingSlotDTO {
    private Integer bookingId;
    private String customerName;
    private String serviceName;
    private String petName;
    private LocalTime startTime;
    private LocalTime endTime;
}

