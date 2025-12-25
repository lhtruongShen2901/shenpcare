package g6shenpcare.models.dto;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.models.entity.PetMedicalRecord;
import lombok.Data;

import java.util.List;

@Data
public class PetMedicalHistoryDTO {
    private Pets pet;
    private CustomerProfile customer;
    private List<MedicalRecordDTO> medicalRecords;
}

