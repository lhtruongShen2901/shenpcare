package g6shenpcare.repository;
import g6shenpcare.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    Optional<ShoppingCart> findByCustomerId(Integer customerId);
}