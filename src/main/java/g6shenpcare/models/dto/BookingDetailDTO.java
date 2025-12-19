package g6shenpcare.models.dto;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import lombok.Data;

@Data
public class BookingDetailDTO {
    private Booking booking;
    private CustomerProfile customer;
    private Pets pet;
//    private PetMedicalRecord medicalRecord;
}