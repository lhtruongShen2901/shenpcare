package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Pets")
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PetId")
    private Integer petId; // Integer

    @Column(name = "PetCode", nullable = false, unique = true, length = 20)
    private String petCode; // P2025xxx

    @Column(name = "OwnerId")
    private Integer ownerId; // Integer (User sở hữu)

    @Column(name = "CustomerId")
    private Integer customerId; // Integer (Profile)

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Species", nullable = false)
    private String species; // DOG, CAT

    @Column(name = "Breed")
    private String breed;

    @Column(name = "Gender")
    private String gender;

    @Column(name = "BirthDate")
    private LocalDate birthDate;

    @Column(name = "WeightKg")
    private Float weightKg;

    // [MỚI] Quan trọng cho tính giá
    @Column(name = "CoatLength")
    private String coatLength; // SHORT, MEDIUM, LONG

    // [MỚI]
    @Column(name = "IsSterilized")
    private boolean sterilized = false;

    @Column(name = "AvatarFileId")
    private Integer avatarFileId;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "IsActive")
    private boolean active = true;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // --- Getters & Setters ---
    public Integer getPetId() { return petId; }
    public void setPetId(Integer petId) { this.petId = petId; }
    public String getPetCode() { return petCode; }
    public void setPetCode(String petCode) { this.petCode = petCode; }
    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }
    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Float getWeightKg() { return weightKg; }
    public void setWeightKg(Float weightKg) { this.weightKg = weightKg; }
    public String getCoatLength() { return coatLength; }
    public void setCoatLength(String coatLength) { this.coatLength = coatLength; }
    public boolean isSterilized() { return sterilized; }
    public void setSterilized(boolean sterilized) { this.sterilized = sterilized; }
    public Integer getAvatarFileId() { return avatarFileId; }
    public void setAvatarFileId(Integer avatarFileId) { this.avatarFileId = avatarFileId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}