package g6shenpcare.repository;

import g6shenpcare.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Integer> {

    // 1. [GIỮ NGUYÊN] Hàm cũ cho CatalogService
    List<ServiceCategory> findByActiveTrue();

    // 2. [GIỮ NGUYÊN] Hàm tìm kiếm chung
    @Query("SELECT c FROM ServiceCategory c WHERE "
            + "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ServiceCategory> searchCategories(@Param("keyword") String keyword);

    // 3. [HÀM MỚI] Tìm theo loại và trạng thái Active
    List<ServiceCategory> findByCategoryTypeAndActiveTrue(String categoryType);

    // 4. [HÀM MỚI] Tìm theo loại (không quan tâm Active)
    List<ServiceCategory> findByCategoryType(String categoryType);

    // 5. [QUAN TRỌNG - NGUYÊN NHÂN LỖI] Phải có @Query ở đây
    @Query("SELECT c FROM ServiceCategory c WHERE "
            + "c.categoryType = :type AND "
            + "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<ServiceCategory> searchClinic(@Param("type") String type, @Param("keyword") String keyword);


    List<ServiceCategory> findByActiveTrueOrderByNameAsc();
}