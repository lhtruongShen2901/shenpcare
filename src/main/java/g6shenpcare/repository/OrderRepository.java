package g6shenpcare.repository;

import g6shenpcare.entity.Order;
import g6shenpcare.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatusIgnoreCase(String status);

    List<Order> findByCustomerId(Integer customerId);
//    Optional<Order> findByOrderNumber(String orderNumber);

        Optional<Order> findByOrderId(Long orderId);
    List<Order> findByStatus(String status);

    List<Order> findByCustomerIdOrderByOrderDateDesc(Integer customerId);
}
