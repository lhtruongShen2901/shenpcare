package g6shenpcare.entity;

import org.hibernate.annotations.Nationalized; // Đã import đúng
import jakarta.persistence.*;

@Entity
@Table(name = "ServiceCategory")
public class ServiceCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceCategoryId")
    private Integer serviceCategoryId;

    // --- SỬA Ở ĐÂY: Thêm @Nationalized ---
    @Nationalized 
    @Column(name = "Name", nullable = false)
    private String name;

    // --- SỬA Ở ĐÂY: Thêm @Nationalized ---
    @Nationalized
    @Column(name = "Description")
    private String description;

    @Column(name = "IsActive")
    private boolean active = true;

    // Getter & Setter
    public Integer getServiceCategoryId() { return serviceCategoryId; }
    public void setServiceCategoryId(Integer id) { this.serviceCategoryId = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}