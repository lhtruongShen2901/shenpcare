package g6shenpcare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Services")
public class Services {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ServiceId")
    private Integer serviceId;

    @Column(name = "ServiceCategoryId")
    @NotNull(message = "Vui lòng chọn danh mục dịch vụ")
    private Integer serviceCategoryId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ServiceCategoryId", insertable = false, updatable = false)
    private ServiceCategory serviceCategory;

    @Column(name = "ImageFileId")
    private Integer imageFileId;

    @Column(name = "Name", nullable = false)
    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Size(max = 100, message = "Tên dịch vụ tối đa 100 ký tự")
    private String name;

    @Column(name = "Description")
    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @Column(name = "DurationMinutes")
    @NotNull(message = "Thời lượng không được để trống")
    @Min(value = 0, message = "Thời lượng tối thiểu 0 phút")
    private Integer durationMinutes = 30;

    @Column(name = "IsActive")
    private Boolean active = true;

    // --- CÁC TRƯỜNG CŨ ---
    @Column(name = "IsCombo")
    private Boolean combo = false;

    @Column(name = "ServiceType", length = 20)
    private String serviceType = "SINGLE"; // SINGLE, COMBO, ADDON

    @Column(name = "TargetSpecies", length = 20)
    private String targetSpecies = "BOTH"; // DOG, CAT, BOTH

    @Column(name = "DiscountPercent")
    private Integer discountPercent = 0;

    @Version
    @Column(name = "Version", nullable = false)
    private Integer version = 0;

    // --- [MỚI] CÁC TRƯỜNG BẮT BUỘC CHO GIAO DIỆN MỚI ---
    // Nếu thiếu các trường này, trang web sẽ bị trắng (Crash)
    @Column(name = "PriceModel", length = 20)
    private String priceModel = "FIXED"; // FIXED, MATRIX, PER_UNIT

    @Column(name = "FixedPrice")
    private BigDecimal fixedPrice = BigDecimal.ZERO;

    @Column(name = "PriceUnit", length = 20)
    private String priceUnit = "LẦN"; // LẦN, KG, GIỜ

    @Column(name = "SortOrder")
    private Integer sortOrder = 0;

    @Column(name = "IsShowWeb")
    private Boolean showOnWeb = true;

    @Column(name = "Tags", length = 200)
    private String tags;

    // Quan hệ Combo
    @OneToMany(mappedBy = "comboServiceId", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ServiceComboItems> comboItems = new ArrayList<>();

    // --- GETTERS & SETTERS (ĐẦY ĐỦ) ---
    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getServiceCategoryId() {
        return serviceCategoryId;
    }

    public void setServiceCategoryId(Integer serviceCategoryId) {
        this.serviceCategoryId = serviceCategoryId;
    }

    public ServiceCategory getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public Integer getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(Integer imageFileId) {
        this.imageFileId = imageFileId;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return active != null && active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCombo() {
        return combo != null && combo;
    }

    public void setCombo(boolean combo) {
        this.combo = combo;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getTargetSpecies() {
        return targetSpecies;
    }

    public void setTargetSpecies(String targetSpecies) {
        this.targetSpecies = targetSpecies;
    }

    public Integer getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(Integer discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // Getters/Setters cho trường mới
    public String getPriceModel() {
        return priceModel;
    }

    public void setPriceModel(String priceModel) {
        this.priceModel = priceModel;
    }

    public BigDecimal getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(BigDecimal fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public String getPriceUnit() {
        return priceUnit;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getShowOnWeb() {
        return showOnWeb;
    }

    public void setShowOnWeb(Boolean showOnWeb) {
        this.showOnWeb = showOnWeb;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<ServiceComboItems> getComboItems() {
        return comboItems;
    }

    public void setComboItems(List<ServiceComboItems> comboItems) {
        this.comboItems = comboItems;
    }
    public boolean isShowOnWeb() { return showOnWeb != null && showOnWeb; }
}
