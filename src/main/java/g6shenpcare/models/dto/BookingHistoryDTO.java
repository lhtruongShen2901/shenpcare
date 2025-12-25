package g6shenpcare.models.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingHistoryDTO {
    private Integer bookingId;
    private LocalDate bookingDate;
    private String status;
    private BigDecimal totalAmount;
    private String serviceName;
    private String petName;
}
