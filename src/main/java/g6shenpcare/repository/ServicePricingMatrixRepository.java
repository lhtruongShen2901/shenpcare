package g6shenpcare.repository;

import g6shenpcare.entity.ServicePricingMatrix;
import org.springframework.data.domain.Pageable; // [MỚI] Import cái này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePricingMatrixRepository extends JpaRepository<ServicePricingMatrix, Integer> {

    // [TỐI ƯU]
    // 1. Bỏ "LIMIT 1" trong câu Query (để tránh lỗi cú pháp SQL Server).
    // 2. Thêm tham số Pageable để Hibernate tự sinh lệnh "TOP 1" hoặc "LIMIT 1" tùy DB.
    // 3. Trả về List (dù chỉ lấy 1) để an toàn hơn, tránh lỗi NonUniqueResultException.
    
    @Query("SELECT p FROM ServicePricingMatrix p " +
           "WHERE p.serviceId = :serviceId " +
           "AND p.petSpecies = :species " +
           "AND (p.coatLength = 'ALL' OR p.coatLength = :coat) " +
           "AND :weight > p.minWeight AND :weight <= p.maxWeight " +
           "ORDER BY p.price DESC") 
    List<ServicePricingMatrix> findMatchingPrice(
            @Param("serviceId") Integer serviceId,
            @Param("species") String species,
            @Param("coat") String coat,
            @Param("weight") Float weight,
            Pageable pageable // [MỚI] Tham số để giới hạn số lượng
    );

    // Bỏ từ khóa public (không cần thiết trong Interface)
    List<ServicePricingMatrix> findByServiceId(Integer serviceId);
}