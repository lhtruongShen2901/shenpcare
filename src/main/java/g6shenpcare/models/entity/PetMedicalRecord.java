package g6shenpcare.models.entity;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PetMedicalRecords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetMedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RecordId")
    private Integer recordId;

    @ManyToOne
    @JoinColumn(name = "PetId", nullable = false)
    private Pets pet;

    @ManyToOne
        @JoinColumn(name = "BookingId")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "DoctorId", nullable = false)
    private StaffProfile doctor;

    @Column(name = "VisitDate", nullable = false)
    private LocalDateTime visitDate = LocalDateTime.now();

    @Column(name = "ExamType", length = 50)
    private String examType;

    @Column(name = "Symptoms", length = 500)
    private String symptoms;

    @Column(name = "PhysicalExamFindings", columnDefinition = "nvarchar(max)")
    private String physicalExamFindings;

    @Column(name = "Diagnosis", length = 500)
    private String diagnosis;

    @Column(name = "FinalDiagnosis", columnDefinition = "nvarchar(max)")
    private String finalDiagnosis;

    @Column(name = "Treatment", length = 500)
    private String treatment;

    @Column(name = "WeightKg")
    private Double weightKg;

    @Column(name = "Temperature")
    private Double temperature;

    @Column(name = "HeartRate")
    private Integer heartRate;

    @Column(name = "MucousMembrane", length = 100)
    private String mucousMembrane;

    @Column(name = "Notes", length = 1000)
    private String notes;

    @Column(name = "NextVisitDate")
    private LocalDate nextVisitDate;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    @OneToMany(mappedBy = "record", fetch = FetchType.LAZY)
    private List<Prescription> prescriptions;

}