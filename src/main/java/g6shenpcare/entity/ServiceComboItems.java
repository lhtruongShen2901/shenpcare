package g6shenpcare.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ServiceComboItems")
public class ServiceComboItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComboItemId")
    private Integer comboItemId;

    @Column(name = "ComboServiceId")
    private Integer comboServiceId;

    @Column(name = "SingleServiceId")
    private Integer singleServiceId;

    // Join để lấy thông tin dịch vụ con (Tên, Giá) hiển thị ra UI
    @ManyToOne
    @JoinColumn(name = "SingleServiceId", insertable = false, updatable = false)
    private Services singleService;

    // Getters & Setters
    public Integer getComboItemId() { return comboItemId; }
    public void setComboItemId(Integer id) { this.comboItemId = id; }
    public Integer getComboServiceId() { return comboServiceId; }
    public void setComboServiceId(Integer id) { this.comboServiceId = id; }
    public Integer getSingleServiceId() { return singleServiceId; }
    public void setSingleServiceId(Integer id) { this.singleServiceId = id; }
    public Services getSingleService() { return singleService; }
}