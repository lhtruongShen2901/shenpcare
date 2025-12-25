package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ServiceHistoryDTO {
    private Integer bookingId;
    private LocalDate bookingDate;
    private String serviceName;
    private String status;
    private BigDecimal amount;
    private String notes;
}
