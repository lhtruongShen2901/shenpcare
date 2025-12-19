package g6shenpcare.models.dto;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Order;
import lombok.Data;

@Data
public class OrderDetailDTO {
    private Order order;
    private CustomerProfile customer;
//    private List<OrderItem> orderItems;
}
