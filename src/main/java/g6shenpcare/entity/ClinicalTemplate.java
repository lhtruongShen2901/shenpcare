package g6shenpcare.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ClinicalTemplates")
@Data // Dùng Lombok cho gọn, hoặc bạn tự generate Getter/Setter
public class ClinicalTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer templateId;

    @Column(nullable = false)
    private String name; // VD: "Mẫu bệnh Viêm Phổi"

    @Column(nullable = false)
    private String type; // SYMPTOMS (Triệu chứng), DIAGNOSIS (Chẩn đoán), ADVICE (Dặn dò)

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String content; // Nội dung mẫu dài

    private Boolean isActive = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}