package g6shenpcare.models.entity;

import g6shenpcare.entity.Order;
import g6shenpcare.entity.Product;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "InventoryTransactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryTransactionId")
    private Integer inventoryTransactionId;

    @ManyToOne
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "QuantityChange", nullable = false)
    private Integer quantityChange;

    @Column(name = "TransactionType", nullable = false, length = 20)
    private String transactionType;

    @ManyToOne
    @JoinColumn(name = "ReferenceOrderId")
    private Order referenceOrder;

    @Column(name = "ReferenceNote", length = 500)
    private String referenceNote;

    @ManyToOne
    @JoinColumn(name = "PerformedByStaffId")
    private StaffProfile performedByStaff;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
