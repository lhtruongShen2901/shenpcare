package g6shenpcare.repository;

import g6shenpcare.entity.Services;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Integer> {
    
    // Đếm số dịch vụ trong 1 danh mục (để cảnh báo khi xóa)
    long countByServiceCategoryId(Integer categoryId);

    // Chuyển toàn bộ dịch vụ từ danh mục cũ sang danh mục mới
    @Modifying
    @Query("UPDATE Services s SET s.serviceCategoryId = :newId WHERE s.serviceCategoryId = :oldId")
    void moveServicesToCategory(Integer oldId, Integer newId);

    // Khóa toàn bộ dịch vụ trong danh mục
    @Modifying
    @Query("UPDATE Services s SET s.active = false WHERE s.serviceCategoryId = :catId")
    void disableServicesByCategory(Integer catId);
}