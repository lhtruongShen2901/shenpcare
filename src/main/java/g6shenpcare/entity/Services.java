package g6shenpcare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Services")
public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceId")
    private Integer serviceId;

    @Column(name = "ServiceCategoryId")
    private Integer serviceCategoryId;
    
    // Join để lấy tên Category hiển thị cho tiện
    @ManyToOne
    @JoinColumn(name = "ServiceCategoryId", insertable = false, updatable = false)
    private ServiceCategory serviceCategory;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "BasePrice")
    private BigDecimal basePrice;

    @Column(name = "DurationMinutes")
    private Integer durationMinutes;

    @Column(name = "IsActive")
    private boolean active = true;

    // Getter & Setter
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer id) { this.serviceId = id; }
    public Integer getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Integer id) { this.serviceCategoryId = id; }
    public ServiceCategory getServiceCategory() { return serviceCategory; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal price) { this.basePrice = price; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer minutes) { this.durationMinutes = minutes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}