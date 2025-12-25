package g6shenpcare.models.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PaymentMethods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentMethodId")
    private Integer paymentMethodId;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "IsOnline", nullable = false)
    private Boolean isOnline = false;
}
