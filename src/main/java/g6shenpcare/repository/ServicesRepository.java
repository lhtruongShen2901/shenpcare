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

    @EntityGraph(attributePaths = {"serviceCategory"})
    @Override
    Page<Services> findAll(Pageable pageable);

    long countByServiceCategoryId(Integer categoryId);

    @Modifying
    @Query("UPDATE Services s SET s.serviceCategoryId = :newId WHERE s.serviceCategoryId = :oldId")
    void moveServicesToCategory(@Param("oldId") Integer oldId, @Param("newId") Integer newId);

    @Modifying
    @Query("UPDATE Services s SET s.active = false WHERE s.serviceCategoryId = :catId")
    void disableServicesByCategory(@Param("catId") Integer catId);

    // --- CÁC HÀM MỚI CHO LOGIC COMBO ---
    // 1. Tìm các dịch vụ KHÔNG PHẢI là loại này (Ví dụ: Lấy tất cả trừ COMBO)
    // SỬA LỖI: Thêm hàm này vào
    List<Services> findByServiceTypeNot(String type);

    // 2. Tìm theo loại (Có phân trang)
    @EntityGraph(attributePaths = {"serviceCategory"})
    Page<Services> findByServiceType(String type, Pageable pageable);

    // Giữ lại hàm cũ để tương thích ngược nếu cần (hoặc xóa đi nếu đã dùng findByServiceTypeNot)
    List<Services> findByActiveTrueAndComboFalse();
    // [MỚI] Tìm dịch vụ theo Category ID (Để hiện lên Modal chọn lọc)

    List<Services> findByServiceCategoryIdAndActiveTrue(Integer categoryId);    // Xóa các hàm findByComboTrue/False cũ vì đã thay bằng findByServiceType
    // Page<Services> findByComboTrue(Pageable pageable); <--- XÓA
    // Page<Services> findByComboFalse(Pageable pageable); <--- XÓA
    // --- [MỚI] Hàm lấy thống kê số lượng: Trả về List Object[] gồm {CategoryId, Count} ---

    @Query("SELECT s.serviceCategoryId, COUNT(s) FROM Services s GROUP BY s.serviceCategoryId")
    List<Object[]> countServicesByCategory();




    List<Services> findByActive(Boolean isActive);
}
