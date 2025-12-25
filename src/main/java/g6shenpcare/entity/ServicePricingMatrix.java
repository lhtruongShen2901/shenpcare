package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ServicePricingMatrix")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServicePricingMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PricingId")
    private Integer pricingId;

    @Column(name = "ServiceId")
    private Integer serviceId;

    @Column(name = "PetSpecies", nullable = false)
    private String petSpecies; // DOG, CAT

    @Column(name = "CoatLength")
    private String coatLength = "ALL"; // SHORT, LONG, ALL

    @Column(name = "MinWeight")
    private Float minWeight = 0f;

    @Column(name = "MaxWeight")
    private Float maxWeight = 999f;

    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;


}