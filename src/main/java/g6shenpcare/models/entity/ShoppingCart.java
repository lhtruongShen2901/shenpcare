package g6shenpcare.models.entity;

import g6shenpcare.entity.CustomerProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ShoppingCarts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartId")
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "CustomerId", nullable = false)
    private CustomerProfile customer;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "LastUpdated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
