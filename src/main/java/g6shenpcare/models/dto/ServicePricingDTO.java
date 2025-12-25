package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ServicePricingDTO {
    private Integer serviceId;
    private String serviceName;
    private String category;
    private BigDecimal basePrice;
    private BigDecimal discountedPrice;
    private Integer discountPercent;
    private String priceModel; // FIXED, WEIGHT_BASED
    private List<PriceRangeDTO> priceRanges;
    private Integer durationMinutes;
    private String description;
}