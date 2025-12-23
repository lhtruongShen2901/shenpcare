package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Pets")
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PetId")
    private Integer petId;

    @Column(name = "PetCode", unique = true, length = 20)
    private String petCode;

    // --- [QUAN TRỌNG] LIÊN KẾT VỚI CUSTOMER PROFILE ---
    @Column(name = "CustomerId")
    private Integer customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CustomerId", insertable = false, updatable = false)
    private CustomerProfile customer;

    @Column(name = "OwnerId") // UserId của bảng Users
    private Integer ownerId;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Species", nullable = false)
    private String species;

    @Column(name = "Breed")
    private String breed;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "WeightKg")
    private Float weightKg;

    @Column(name = "CoatLength")
    private String coatLength;

    // --- [MỚI] CÁC TRƯỜNG BỔ SUNG ---
    @Column(name = "Color")
    private String color;

    @Column(name = "MicrochipNumber")
    private String microchipNumber;

    @Column(name = "DistinguishingMarks")
    private String distinguishingMarks;
    // --------------------------------

    @Column(name = "IsSterilized")
    private boolean sterilized = false;

    @Column(name = "AvatarFileId")
    private Integer avatarFileId;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "IsActive")
    private boolean active = true;

    // --- Liên kết để xem lịch sử đặt lịch của Pet ---
    @OneToMany(mappedBy = "pet", fetch = FetchType.LAZY)
    private List<Booking> bookings;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    public Pets() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.petCode == null) {
            this.petCode = "PET" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Helpers ---
    public void setWeight(Double weight) {
        this.weightKg = (weight != null) ? weight.floatValue() : null;
    }

    public Double getWeight() {
        return (weightKg != null) ? weightKg.doubleValue() : null;
    }

    // --- Getters & Setters ---
    public Integer getPetId() {
        return petId;
    }

    public void setPetId(Integer petId) {
        this.petId = petId;
    }

    public String getPetCode() {
        return petCode;
    }

    public void setPetCode(String petCode) {
        this.petCode = petCode;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public CustomerProfile getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerProfile customer) {
        this.customer = customer;
        if (customer != null) {
            this.customerId = customer.getCustomerId();
        }
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Float getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Float weightKg) {
        this.weightKg = weightKg;
    }

    public String getCoatLength() {
        return coatLength;
    }

    public void setCoatLength(String coatLength) {
        this.coatLength = coatLength;
    }

    // --- [MỚI] GETTER/SETTER CHO 3 TRƯỜNG MỚI ---
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMicrochipNumber() {
        return microchipNumber;
    }

    public void setMicrochipNumber(String microchipNumber) {
        this.microchipNumber = microchipNumber;
    }

    public String getDistinguishingMarks() {
        return distinguishingMarks;
    }

    public void setDistinguishingMarks(String distinguishingMarks) {
        this.distinguishingMarks = distinguishingMarks;
    }
    // --------------------------------------------

    public boolean isSterilized() {
        return sterilized;
    }

    public void setSterilized(boolean sterilized) {
        this.sterilized = sterilized;
    }

    public Integer getAvatarFileId() {
        return avatarFileId;
    }

    public void setAvatarFileId(Integer avatarFileId) {
        this.avatarFileId = avatarFileId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
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

    public boolean getSterilized() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
