package g6shenpcare.models.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MedicalRecordDTO {
    private Integer recordId;
    private LocalDateTime visitDate;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private Float weight;
    private Float temperature;
    private String symptoms;
    private String notes;
}
