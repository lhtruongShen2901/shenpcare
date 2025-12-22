package g6shenpcare.repository;

import g6shenpcare.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    // Tìm các món hàng thuộc về một đơn hàng cụ thể
    List<OrderItems> findByOrder_OrderId(Long orderId);
}