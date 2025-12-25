package g6shenpcare.models.entity;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.Services;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Vaccinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VaccinationId")
    private Integer vaccinationId;

    @ManyToOne
    @JoinColumn(name = "PetId", nullable = false)
    private Pets pet;

    @ManyToOne
    @JoinColumn(name = "BookingId")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "ServiceId")
    private Services service;

    @Column(name = "VaccineName", nullable = false, length = 200)
    private String vaccineName;

    @Column(name = "VaccineType", nullable = false, length = 50)
    private String vaccineType;

    @Column(name = "BatchNumber", length = 50)
    private String batchNumber;

    @Column(name = "Manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "AdministeredDate")
    private LocalDateTime administeredDate = LocalDateTime.now();

    @Column(name = "NextDueDate")
    private LocalDate nextDueDate;

    @ManyToOne
    @JoinColumn(name = "PerformedByStaffId")
    private StaffProfile performedByStaff;

    @Column(name = "Notes", length = 500)
    private String notes;
}