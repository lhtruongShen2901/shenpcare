package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PetDetailDTO {
    private Integer petId;
    private String name;
    private String species;
    private String breed;
    private String gender;
    private LocalDate birthDate;
    private Float weightKg;
    private String color;
    private String coatLength;
    private Boolean isSterilized;
    private String notes;
    private Integer totalVisits;
    private LocalDate lastVisit;
    private int recentBookingCount;
}
