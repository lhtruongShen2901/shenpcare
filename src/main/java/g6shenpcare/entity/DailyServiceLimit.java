package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "DailyServiceLimit")
public class DailyServiceLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer limitId;

    @Column(name = "ServiceType", nullable = false, length = 50)
    private String serviceType; // Ví dụ: 'SPA', 'MEDICAL'

    @Column(name = "ApplyDate")
    private LocalDate applyDate; // Nếu NULL thì là cấu hình Mặc Định

    @Column(name = "MaxQuota", nullable = false)
    private Integer maxQuota;

    @Column(name = "Note", length = 255)
    private String note;

    // --- Getters & Setters ---
    public Integer getLimitId() { return limitId; }
    public void setLimitId(Integer limitId) { this.limitId = limitId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public LocalDate getApplyDate() { return applyDate; }
    public void setApplyDate(LocalDate applyDate) { this.applyDate = applyDate; }
    public Integer getMaxQuota() { return maxQuota; }
    public void setMaxQuota(Integer maxQuota) { this.maxQuota = maxQuota; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}