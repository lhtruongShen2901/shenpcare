package g6shenpcare.models.dto;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Order;
import g6shenpcare.entity.Pets;
import g6shenpcare.models.entity.Ticket;
import lombok.Data;

import java.util.List;

@Data
public class LookupResultDTO {
    private CustomerProfile customer;
    private List<Pets> pets;
//    private List<PetMedicalRecord> medicalRecords;
    private List<Booking> bookings;
    private List<Order> orders;
    private List<Ticket> supportTickets;
}
