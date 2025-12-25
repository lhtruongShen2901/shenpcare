package g6shenpcare.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ProductCategory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductCategoryId")
    private Integer productCategoryId;

    @Column(name = "Name", nullable = false, length = 100)
    private String name;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;
}