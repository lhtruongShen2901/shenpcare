package g6shenpcare.models.dto;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.Services;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroomerPetInfoDTO {
    private Booking booking;
    private Pets pet;
    private CustomerProfile customer;
    private Services service;
    private String petAge;
    private List<PreviousVisitDTO> previousVisits;
}
