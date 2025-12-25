package g6shenpcare.models.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PaymentConfigs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentConfigId")
    private Integer paymentConfigId;

    @ManyToOne
    @JoinColumn(name = "PaymentMethodId", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "ProviderName", nullable = false, length = 100)
    private String providerName;

    @Column(name = "PartnerCode", length = 100)
    private String partnerCode;

    @Column(name = "AccessKey", length = 100)
    private String accessKey;

    @Column(name = "SecretKey", length = 100)
    private String secretKey;

    @Column(name = "CallbackUrl", length = 255)
    private String callbackUrl;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CreatedAt", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;
}
