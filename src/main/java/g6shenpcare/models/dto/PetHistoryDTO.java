package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PetHistoryDTO {
    private PetDetailDTO pet;
    private List<ServiceHistoryDTO> serviceHistory;
    private List<MedicalRecordDTO> medicalHistory;
}
