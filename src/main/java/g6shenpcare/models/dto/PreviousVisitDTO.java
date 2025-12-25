package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreviousVisitDTO {
    private Integer bookingId;
    private LocalDateTime startTime;
    private String serviceName;
    private String status;
}