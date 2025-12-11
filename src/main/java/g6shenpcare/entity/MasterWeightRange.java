package g6shenpcare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "MasterWeightRanges")
public class MasterWeightRange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rangeId;

    private String species; // DOG, CAT
    private String rangeName;
    private Float minWeight;
    private Float maxWeight;
    private boolean isActive = true;

    // Getters & Setters
    public Integer getRangeId() { return rangeId; }
    public void setRangeId(Integer rangeId) { this.rangeId = rangeId; }
    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }
    public String getRangeName() { return rangeName; }
    public void setRangeName(String rangeName) { this.rangeName = rangeName; }
    public Float getMinWeight() { return minWeight; }
    public void setMinWeight(Float minWeight) { this.minWeight = minWeight; }
    public Float getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Float maxWeight) { this.maxWeight = maxWeight; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}