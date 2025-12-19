package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderId")
    private Long orderId;

    // change to Long to match typical ID types Integer
    @Column(name = "CustomerId", nullable = false)
    private Integer customerId;

    @Column(name = "OrderDate", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "Status", nullable = false, length = 20)
    private String status;   // PENDING, CONFIRMED, ...

    @Column(name = "ShippingAddress", length = 255)
    private String shippingAddress;

    @Column(name = "TotalAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "Notes", length = 500)
    private String notes;

    public Order() {
    }


}
