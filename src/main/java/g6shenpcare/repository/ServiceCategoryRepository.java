package g6shenpcare.repository;

import g6shenpcare.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Integer> {
    
    List<ServiceCategory> findByActiveTrue(); 

    // [MỚI] Hàm tìm kiếm danh mục
    @Query("SELECT c FROM ServiceCategory c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ServiceCategory> searchCategories(@Param("keyword") String keyword);
}