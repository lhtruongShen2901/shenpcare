package g6shenpcare.models.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GroomerScheduleDTO {
    private Integer bookingId;
    private LocalDate bookingDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String notes;

    // Customer info
    private String customerName;
    private String customerPhone;

    // Pet info
    private String petName;
    private String petSpecies;
    private String petBreed;
    private Double petWeightKg;

    // Service info
    private String serviceName;
    private String serviceCategory;
    private Integer durationMinutes;
    private BigDecimal totalAmount;

    // Progress info
    private String lastProgressNotes;
    private LocalDateTime lastProgressTime;

    // Helper methods
    public String getStatusBadgeClass() {
        switch (status) {
            case "PENDING":
                return "bg-yellow-100 text-yellow-800";
            case "CONFIRMED":
                return "bg-blue-100 text-blue-800";
            case "IN_PROGRESS":
                return "bg-purple-100 text-purple-800";
            case "COMPLETED":
                return "bg-green-100 text-green-800";
            case "CANCELLED":
                return "bg-red-100 text-red-800";
            default:
                return "bg-gray-100 text-gray-800";
        }
    }

    public String getStatusText() {
        switch (status) {
            case "PENDING":
                return "Chờ xử lý";
            case "CONFIRMED":
                return "Đã xác nhận";
            case "IN_PROGRESS":
                return "Đang thực hiện";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    public String getServiceTypeIcon() {
        String categoryLower = serviceCategory.toLowerCase();
        if (categoryLower.contains("boarding") || categoryLower.contains("trông")) {
            return "🏠";
        } else if (categoryLower.contains("spa") || categoryLower.contains("grooming")) {
            return "✂️";
        }
        return "🐾";
    }
}