package g6shenpcare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PrescriptionDetails")
public class PrescriptionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recordId")
    private MedicalRecord medicalRecord;

    @ManyToOne
    @JoinColumn(name = "productId") // Liên kết trực tiếp với Kho của Admin
    private Product product;

    private int quantity; // Số lượng xuất kho
    
    @Column(columnDefinition = "NVARCHAR(255)")
    private String usageInstruction; // Cách dùng (Sáng/Chiều/Tối)

    // --- GETTERS & SETTERS ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MedicalRecord getMedicalRecord() { return medicalRecord; }
    public void setMedicalRecord(MedicalRecord medicalRecord) { this.medicalRecord = medicalRecord; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUsageInstruction() { return usageInstruction; }
    public void setUsageInstruction(String usageInstruction) { this.usageInstruction = usageInstruction; }
}