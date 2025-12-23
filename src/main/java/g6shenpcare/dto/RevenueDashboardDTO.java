package g6shenpcare.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueDashboardDTO {
    // 1. Tổng quan
    private BigDecimal totalRevenueToday;
    private BigDecimal totalSpaRevenue;
    private BigDecimal totalClinicRevenue; // Thuốc kê đơn + Tiền khám
    private BigDecimal totalOnlineRevenue;

    // 2. Chi tiết hiệu suất nhân viên (List cũ của bạn)
    private List<DailyReportDTO> doctorPerformance; 

    // Constructor, Getters, Setters
    public RevenueDashboardDTO() {}

    public BigDecimal getTotalRevenueToday() { return totalRevenueToday; }
    public void setTotalRevenueToday(BigDecimal totalRevenueToday) { this.totalRevenueToday = totalRevenueToday; }

    public BigDecimal getTotalSpaRevenue() { return totalSpaRevenue; }
    public void setTotalSpaRevenue(BigDecimal totalSpaRevenue) { this.totalSpaRevenue = totalSpaRevenue; }

    public BigDecimal getTotalClinicRevenue() { return totalClinicRevenue; }
    public void setTotalClinicRevenue(BigDecimal totalClinicRevenue) { this.totalClinicRevenue = totalClinicRevenue; }

    public BigDecimal getTotalOnlineRevenue() { return totalOnlineRevenue; }
    public void setTotalOnlineRevenue(BigDecimal totalOnlineRevenue) { this.totalOnlineRevenue = totalOnlineRevenue; }

    public List<DailyReportDTO> getDoctorPerformance() { return doctorPerformance; }
    public void setDoctorPerformance(List<DailyReportDTO> doctorPerformance) { this.doctorPerformance = doctorPerformance; }
}