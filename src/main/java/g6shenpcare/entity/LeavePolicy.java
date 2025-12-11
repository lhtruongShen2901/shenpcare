package g6shenpcare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LeavePolicies")
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer policyId;

    @Column(unique = true, nullable = false)
    private String roleName;

    private Integer maxDaysPerMonth;    // Giới hạn tháng
    private Integer defaultAnnualQuota; // Định mức năm (để reset)

    // Getters & Setters
    public Integer getPolicyId() { return policyId; }
    public void setPolicyId(Integer policyId) { this.policyId = policyId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Integer getMaxDaysPerMonth() { return maxDaysPerMonth; }
    public void setMaxDaysPerMonth(Integer maxDaysPerMonth) { this.maxDaysPerMonth = maxDaysPerMonth; }
    public Integer getDefaultAnnualQuota() { return defaultAnnualQuota; }
    public void setDefaultAnnualQuota(Integer defaultAnnualQuota) { this.defaultAnnualQuota = defaultAnnualQuota; }
}