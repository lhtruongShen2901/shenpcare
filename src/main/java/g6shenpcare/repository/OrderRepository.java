package g6shenpcare.repository;

import g6shenpcare.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatusIgnoreCase(String status);
}
