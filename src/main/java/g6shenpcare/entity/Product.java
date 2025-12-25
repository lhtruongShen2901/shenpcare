package g6shenpcare.entity;

import g6shenpcare.models.entity.ProductCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "Products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId")
    private Integer productId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductCategoryId", nullable = false)
    private ProductCategory category;

    @Column(name = "Name", length = 255)
    private String name;

    @Column(name = "Description", length = 500)
    private String description;

    @Column(name = "UnitPrice", precision = 38, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "Unit", length = 255)
    private String unit;

    @Column(name = "IsMedicine", nullable = false)
    private Boolean isMedicine;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "AlertThreshold")
    private Integer alertThreshold;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Column(name = "ImageFileId")
    private Long imageFileId;

    @Column(name = "ImageUrl", length = 255)
    private String imageUrl;

    @Column(name = "ImportPrice", precision = 38, scale = 2)
    private BigDecimal importPrice;

    @Column(name = "Ingredient", columnDefinition = "nvarchar(max)")
    private String ingredient;

    @Column(name = "RetailPrice", precision = 38, scale = 2)
    private BigDecimal retailPrice;

    @Column(name = "Sku", length = 255)
    private String sku;

    @Column(name = "StockQuantity")
    private Integer stockQuantity;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "Usage", columnDefinition = "nvarchar(max)")
    private String usage;

    @Column(name = "IsPrescription")
    private Boolean isPrescription;

    @Column(name = "ProductForm", length = 255)
    private String productForm;

    @Column(name = "TargetSpecies", length = 255)
    private String targetSpecies;
}
