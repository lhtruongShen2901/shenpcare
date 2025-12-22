package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "StaffProfile")
public class StaffProfile {

    @Id
    @Column(name = "StaffId")
    private Integer staffId;   // Maps with Users.UserId

    @Column(name = "StaffCode", length = 50, nullable = false)
    private String staffCode;

    @Column(name = "StaffType", length = 50)
    private String staffType;    // ADMIN / DOCTOR / GROOMER / ...

    // [MỚI] Thêm trường Position (Chức vụ)
    @Column(name = "Position", length = 100)
    private String position;

    @Column(name = "LicenseNumber", length = 100)
    private String licenseNumber;

    @Column(name = "Specialization", length = 255)
    private String specialization;

    @Column(name = "HireDate")
    private LocalDate hireDate;

    // Hạn mức phép năm (Mặc định 12 ngày)
    @Column(name = "AnnualLeaveQuota")
    private Integer annualLeaveQuota = 12;

    public StaffProfile() {
    }

    // --- Getter & Setter ---

    public Integer getStaffId() { return staffId; }
    public void setStaffId(Integer staffId) { this.staffId = staffId; }

    public String getStaffCode() { return staffCode; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }

    public String getStaffType() { return staffType; }
    public void setStaffType(String staffType) { this.staffType = staffType; }

    // [MỚI] Getter & Setter cho Position
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public Integer getAnnualLeaveQuota() { return annualLeaveQuota; }
    public void setAnnualLeaveQuota(Integer annualLeaveQuota) { this.annualLeaveQuota = annualLeaveQuota; }
}