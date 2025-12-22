package g6shenpcare.dto;

import java.math.BigDecimal;

public class DailyReportDTO {
    private Long doctorId;
    private Long patientCount;  // Số bệnh nhân khám
    private Long medicineCount; // Tổng số viên thuốc kê
    private BigDecimal estimatedRevenue; // Doanh thu dự kiến

    public DailyReportDTO(Long doctorId, Long patientCount, Long medicineCount, BigDecimal estimatedRevenue) {
        this.doctorId = doctorId;
        this.patientCount = patientCount;
        this.medicineCount = medicineCount;
        this.estimatedRevenue = estimatedRevenue;
    }

    // Getters
    public Long getDoctorId() { return doctorId; }
    public Long getPatientCount() { return patientCount; }
    public Long getMedicineCount() { return medicineCount; }
    public BigDecimal getEstimatedRevenue() { return estimatedRevenue; }
}