package g6shenpcare.repository;

import g6shenpcare.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    // Lấy tất cả thuốc đang hoạt động
    List<Product> findByIsMedicineTrueAndIsActiveTrue();

    // Tìm kiếm thuốc theo tên hoặc hoạt chất
    @Query("SELECT p FROM Product p WHERE p.isMedicine = true AND (p.name LIKE %:keyword% OR p.ingredient LIKE %:keyword% OR p.sku LIKE %:keyword%)")
    List<Product> searchMedicines(@Param("keyword") String keyword);
    
    // Lọc theo loại (Vaccine, Medicine...)
    List<Product> findByCategoryAndIsActiveTrue(String category);
}