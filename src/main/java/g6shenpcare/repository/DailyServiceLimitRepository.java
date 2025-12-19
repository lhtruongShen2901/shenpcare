package g6shenpcare.repository;

import g6shenpcare.entity.DailyServiceLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyServiceLimitRepository extends JpaRepository<DailyServiceLimit, Integer> {

    // Tìm cấu hình cho một ngày cụ thể
    Optional<DailyServiceLimit> findByApplyDateAndServiceType(LocalDate applyDate, String serviceType);

    // Tìm cấu hình mặc định (ApplyDate is NULL)
    Optional<DailyServiceLimit> findByServiceTypeAndApplyDateIsNull(String serviceType);
}