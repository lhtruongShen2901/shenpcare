package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderDTO {
    private Long orderId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private Integer itemCount;
    private String shippingAddress;
}
