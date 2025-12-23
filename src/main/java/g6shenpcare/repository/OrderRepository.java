package g6shenpcare.repository;

import g6shenpcare.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatusIgnoreCase(String status);

    public List<Order> findByCustomerIdOrderByOrderDateDesc(Long valueOf);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o "
            + "WHERE CAST(o.orderDate AS date) = :today AND o.status <> 'CANCELLED'")
    BigDecimal calculateDailyOnlineRevenue(@Param("today") LocalDate today);
}
