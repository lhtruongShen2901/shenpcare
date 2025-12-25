package g6shenpcare.models.dto;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Order;
import g6shenpcare.models.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailDTO {
    private Order order;
    private CustomerProfile customer;
    private List<OrderItem> orderItems;
    private Integer orderId;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private List<OrderItemDTO> items;
    private String notes;
}
