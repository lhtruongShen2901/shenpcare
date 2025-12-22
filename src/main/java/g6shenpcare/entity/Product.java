package g6shenpcare.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String name;        // Tên thuốc / Vật tư

    private String sku;         // Mã Barcode/SKU

    // --- SỬA ĐỔI QUAN TRỌNG TẠI ĐÂY ---
    // Trước đây bạn dùng @ManyToOne nhưng HTML lại gửi String. 
    // Chúng ta sửa thành String để khớp với form.
    @Column(name = "category", length = 50)
    private String category;    // MEDICINE, VACCINE, CONSUMABLE

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String usage;       // Công dụng / Chỉ định 

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String ingredient;  // Hoạt chất chính

    // --- CÁC TRƯỜNG MỚI THÊM ---
    private String targetSpecies; // DOG, CAT, BOTH
    private String productForm;   // TABLET, LIQUID...
    private Boolean isPrescription; // True/False
    
    // Quản lý Giá & Đơn vị
    private String unit;            // Đơn vị tính
    private BigDecimal importPrice; // Giá nhập vào
    private BigDecimal retailPrice; // Giá bán lẻ 

    // Quản lý Kho trực tiếp
    private Integer stockQuantity = 0;
    private Integer alertThreshold = 10;
    private LocalDate expiryDate;

    // Hình ảnh minh họa
    private String imageUrl;
    private Long imageFileId;

    // Trạng thái
    private Boolean isActive = true;
    private Boolean isMedicine = true;

    // Audit
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // GETTER & SETTER
    // ==========================================
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    // --- SỬA LẠI GETTER/SETTER CHO CATEGORY ---
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    // ------------------------------------------

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(Integer alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(Long imageFileId) {
        this.imageFileId = imageFileId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsMedicine() {
        return isMedicine;
    }

    public void setIsMedicine(Boolean isMedicine) {
        this.isMedicine = isMedicine;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTargetSpecies() {
        return targetSpecies;
    }

    public void setTargetSpecies(String targetSpecies) {
        this.targetSpecies = targetSpecies;
    }

    public String getProductForm() {
        return productForm;
    }

    public void setProductForm(String productForm) {
        this.productForm = productForm;
    }

    public Boolean getIsPrescription() {
        return isPrescription;
    }

    public void setIsPrescription(Boolean isPrescription) {
        this.isPrescription = isPrescription;
    }
}