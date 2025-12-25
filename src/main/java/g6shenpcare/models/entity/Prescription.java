package g6shenpcare.models.entity;

import g6shenpcare.entity.StaffProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionId")
    private Integer prescriptionId;

    @ManyToOne
    @JoinColumn(name = "RecordId", nullable = false)
    private PetMedicalRecord record;

    @ManyToOne
    @JoinColumn(name = "DoctorId", nullable = false)
    private StaffProfile doctor;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "Notes", length = 500)
    private String notes;

    @OneToMany(
            mappedBy = "prescription",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PrescriptionItem> items;
}
