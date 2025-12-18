package g6shenpcare.repository;

import g6shenpcare.entity.Services;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Integer> {

    // Override findAll để tối ưu fetch category (tránh N+1)
    @EntityGraph(attributePaths = {"serviceCategory"})
    @Override
    Page<Services> findAll(Pageable pageable);

    // =================================================================
    // CÁC HÀM ĐẾM & KIỂM TRA
    // =================================================================
    
    // Đếm số dịch vụ trong một danh mục (Dùng cho logic xóa Category)
    @Query("SELECT COUNT(s) FROM Services s WHERE s.serviceCategory.serviceCategoryId = :catId")
    long countByServiceCategoryId(@Param("catId") Integer categoryId);

    // Alias cho hàm trên để code cũ không lỗi (giữ tương thích)
    @Query("SELECT COUNT(s) FROM Services s WHERE s.serviceCategory.serviceCategoryId = :catId")
    long countServicesInClinic(@Param("catId") Integer categoryId);

    // Thống kê số lượng dịch vụ theo từng Category (Dùng cho Dashboard/List)
    @Query("SELECT s.serviceCategoryId, COUNT(s) FROM Services s GROUP BY s.serviceCategoryId")
    List<Object[]> countServicesByCategory();

    // =================================================================
    // CÁC HÀM TÌM KIẾM & LỌC
    // =================================================================

    // Lấy dịch vụ không phải loại hình này (VD: Lấy Single để add vào Combo)
    List<Services> findByServiceTypeNot(String type);

    // Lấy dịch vụ theo loại hình (SINGLE, COMBO) có phân trang
    @EntityGraph(attributePaths = {"serviceCategory"})
    Page<Services> findByServiceType(String type, Pageable pageable);

    // Lấy tất cả dịch vụ đang hoạt động trong một Category cụ thể
    List<Services> findByServiceCategoryIdAndActiveTrue(Integer categoryId);
    
    // Lấy dịch vụ lẻ đang hoạt động (cho Dropdown tạo Combo)
    List<Services> findByActiveTrueAndComboFalse();

    // [QUAN TRỌNG] Lấy dịch vụ theo Hệ thống (SPA hoặc CLINIC) dựa vào Category Type
    @Query("SELECT s FROM Services s JOIN s.serviceCategory c WHERE c.categoryType = :catType")
    @EntityGraph(attributePaths = {"serviceCategory"})
    Page<Services> findByCategoryType(@Param("catType") String categoryType, Pageable pageable);

    // =================================================================
    // CÁC HÀM CẬP NHẬT HÀNG LOẠT (BULK UPDATE)
    // =================================================================

    @Modifying
    @Query("UPDATE Services s SET s.serviceCategoryId = :newId WHERE s.serviceCategoryId = :oldId")
    void moveServicesToCategory(@Param("oldId") Integer oldId, @Param("newId") Integer newId);

    @Modifying
    @Query("UPDATE Services s SET s.active = false WHERE s.serviceCategoryId = :catId")
    void disableServicesByCategory(@Param("catId") Integer catId);
}