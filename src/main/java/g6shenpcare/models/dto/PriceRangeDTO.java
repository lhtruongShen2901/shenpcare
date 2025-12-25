package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceRangeDTO {
    private Float minWeight;
    private Float maxWeight;
    private String coatLength;
    private BigDecimal price;
}
