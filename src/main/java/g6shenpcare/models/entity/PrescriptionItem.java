package g6shenpcare.models.entity;

import g6shenpcare.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PrescriptionItems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PrescriptionItemId")
    private Integer prescriptionItemId;

    @ManyToOne
    @JoinColumn(name = "PrescriptionId", nullable = false)
    private Prescription prescription;

    @Column(name = "MedicineName", nullable = false, length = 200)
    private String medicineName;

    @ManyToOne
    @JoinColumn(name = "ProductId")
    private Product product;

    @Column(name = "Dosage", length = 100)
    private String dosage;

    @Column(name = "Frequency", length = 100)
    private String frequency;

    @Column(name = "DurationDays")
    private Integer durationDays;

    @Column(name = "Instruction", length = 500)
    private String instruction;

    @Column(name = "Notes", length = 500)
    private String notes;
}