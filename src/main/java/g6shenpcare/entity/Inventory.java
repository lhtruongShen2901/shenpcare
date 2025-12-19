package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inventory")
@Data
public class Inventory {
    @Id
    @Column(name = "ProductId")
    private Integer productId;

    // Chia sẻ khóa chính với Product (1 Product có 1 Inventory)
    @OneToOne
    @MapsId
    @JoinColumn(name = "ProductId")
    private Product product;

    private Integer quantityInStock = 0;
    private Integer reorderLevel = 10; // Mức cảnh báo nhập hàng
    private LocalDateTime lastUpdated = LocalDateTime.now();
}