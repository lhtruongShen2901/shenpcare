package g6shenpcare.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "StaffProfile")
public class StaffProfile {

    @Id
    @Column(name = "StaffId")
    private Long staffId;   // Maps with Users.UserId

    @Column(name = "StaffCode", length = 50, nullable = false)
    private String staffCode;

    @Column(name = "StaffType", length = 50)
    private String staffType;    // ADMIN / DOCTOR / GROOMER / ...

    @Column(name = "LicenseNumber", length = 100)
    private String licenseNumber;

    @Column(name = "Specialization", length = 255)
    private String specialization;

    @Column(name = "HireDate")
    private LocalDate hireDate;

    public StaffProfile() {
    }

    // --- Getter & Setter ---

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(String staffCode) {
        this.staffCode = staffCode;
    }

    public String getStaffType() {
        return staffType;
    }

    public void setStaffType(String staffType) {
        this.staffType = staffType;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }
}