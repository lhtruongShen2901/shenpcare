package g6shenpcare.entity;
//lien quan service
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ServicePricingMatrix")
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

    // GETTERS & SETTERS
    public Integer getPricingId() { return pricingId; }
    public void setPricingId(Integer pricingId) { this.pricingId = pricingId; }

    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }

    public String getPetSpecies() { return petSpecies; }
    public void setPetSpecies(String petSpecies) { this.petSpecies = petSpecies; }

    public String getCoatLength() { return coatLength; }
    public void setCoatLength(String coatLength) { this.coatLength = coatLength; }

    public Float getMinWeight() { return minWeight; }
    public void setMinWeight(Float minWeight) { this.minWeight = minWeight; }

    public Float getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Float maxWeight) { this.maxWeight = maxWeight; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}