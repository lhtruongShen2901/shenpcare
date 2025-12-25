package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "StaffProfile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfile {
    @Id
    @Column(name = "StaffId")
    private Integer staffId;

    @OneToOne
    @JoinColumn(name = "UserId", unique = true)
    private UserAccount user;

    @Column(name = "StaffCode", nullable = false, length = 50)
    private String staffCode;

    @Column(name = "StaffType", length = 50)
    private String staffType;

    @Column(name = "Position", length = 100)
    private String position;

    @Column(name = "LicenseNumber", length = 100)
    private String licenseNumber;

    @Column(name = "Specialization", length = 255)
    private String specialization;

    @Column(name = "HireDate")
    private java.time.LocalDate hireDate;

    @Column(name = "BranchId")
    private Integer branchId;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Notes", length = 500)
    private String notes;

    @Column(name = "AnnualLeaveQuota", nullable = false)
    private Integer annualLeaveQuota = 12;
}