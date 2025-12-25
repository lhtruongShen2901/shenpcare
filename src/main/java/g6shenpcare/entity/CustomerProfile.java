package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CustomerProfile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile {
    @Id
    @Column(name = "CustomerId")
    private Integer customerId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "CustomerId")
    private UserAccount user;

    @Column(name = "UserId", nullable = false)
    private Long userId;

    @Column(name = "FullName", nullable = false, length = 100)
    private String fullName;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "DefaultAddress", length = 255)
    private String defaultAddress;

    @Column(name = "AddressLine", length = 255)
    private String addressLine;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "District", length = 100)
    private String district;

    @Column(name = "Ward", length = 100)
    private String ward;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
