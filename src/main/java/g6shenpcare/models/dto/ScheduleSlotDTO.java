package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ScheduleSlotDTO {
    private Integer staffId;
    private String staffName;
    private String staffType;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer currentBookings;
    private Integer maxBookings;
    private Boolean available;
    private List<BookingSlotDTO> bookings;
}
