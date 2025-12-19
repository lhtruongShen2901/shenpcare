package g6shenpcare.entity;

import org.hibernate.annotations.Nationalized; // Đã import đúng
import jakarta.persistence.*;
import java.util.List;

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

    @Nationalized
    @Column(name = "Description", columnDefinition = "nvarchar(max)") // Thêm dòng này
    private String description;

    @Column(name = "IsActive")
    private boolean active = true;

    // [MỚI] Thêm ảnh đại diện
    @Column(name = "ImageFileId")
    private Integer imageFileId;

    @Column(name = "CategoryType")
    private String categoryType; // Giá trị: 'SPA', 'CLINIC', 'BOARDING'

    @Column(name = "IconUrl")
    private String iconUrl; // Đường dẫn ảnh icon (VD: /img/specialties/xray.png)

    // Relationship với Services
    @OneToMany(mappedBy = "serviceCategory")
    private List<Services> services;

    // Getter & Setter
    public Integer getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(Integer id) {
        this.serviceCategoryId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(Integer imageFileId) {
        this.imageFileId = imageFileId;
    }
    // --- CÁC METHOD CẦN BỔ SUNG ---

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<Services> getServices() {
        return services;
    }

    public void setServices(List<Services> services) {
        this.services = services;
    }

    // [QUAN TRỌNG] Thêm hàm này để khớp với lệnh specialty.setIsActive(true) trong Controller
    public void setIsActive(boolean isActive) {
        this.active = isActive;
    }

}
