package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Pets")
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PetId")
    private Integer petId;

    @Column(name = "PetCode", nullable = false, unique = true, length = 20)
    private String petCode; // P2025xxx

    @Column(name = "OwnerId")
    private Integer ownerId; // User sở hữu

    @Column(name = "CustomerId", nullable = false)
    private Integer customerId; // CustomerProfile

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

    @Column(name = "DateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "Color", length = 50)
    private String color;

    @Column(name = "WeightKg")
    private Float weightKg;

    // Quan trọng cho tính giá
    @Column(name = "CoatLength")
    private String coatLength; // SHORT, MEDIUM, LONG

    @Column(name = "IsSterilized", nullable = false)
    private boolean sterilized = false;

    @Column(name = "AvatarFileId")
    private Integer avatarFileId;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "MicrochipNumber", length = 50)
    private String microchipNumber;

    @Column(name = "DistinguishingMarks", length = 255)
    private String distinguishingMarks;

    @Column(name = "ColorPattern", length = 50)
    private String colorPattern;

    @Column(name = "IsActive", nullable = false)
    private boolean active = true;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;
}
