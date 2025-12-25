package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class TimeSlotDTO {
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean available;
    private Integer staffId;
    private String staffName;
    private Integer currentBookings;
    private Integer maxBookings;
}
