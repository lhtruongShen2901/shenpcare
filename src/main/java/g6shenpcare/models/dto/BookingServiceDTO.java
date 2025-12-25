package g6shenpcare.models.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingServiceDTO {
    private Integer serviceId;
    private String serviceName;
    private BigDecimal fixedPrice;
}
