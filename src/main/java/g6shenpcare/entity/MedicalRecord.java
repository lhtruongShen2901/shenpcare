package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "MedicalRecords")
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    private String bookingId; // Mã cuộc hẹn
    private Long doctorId;    // ID bác sĩ thực hiện (để Admin chấm công/báo cáo)
    private Long petId;       // ID thú cưng

    // Kết quả khám
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String symptoms; // Triệu chứng
    
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String diagnosis; // Chẩn đoán

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String doctorNotes; // Lời dặn dò

    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Status: COMPLETED (Khám xong), PAID (Đã thu tiền)
    private String status = "COMPLETED"; 

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<PrescriptionDetail> prescriptionDetails;

    // --- GETTERS & SETTERS ---
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<PrescriptionDetail> getPrescriptionDetails() { return prescriptionDetails; }
    public void setPrescriptionDetails(List<PrescriptionDetail> prescriptionDetails) { this.prescriptionDetails = prescriptionDetails; }
}