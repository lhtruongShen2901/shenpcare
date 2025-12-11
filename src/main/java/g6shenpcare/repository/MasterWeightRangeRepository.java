package g6shenpcare.repository;

import g6shenpcare.entity.MasterWeightRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MasterWeightRangeRepository extends JpaRepository<MasterWeightRange, Integer> {
    // Lấy danh sách theo loài để hiển thị lên Dropdown
    List<MasterWeightRange> findBySpeciesAndIsActiveTrue(String species);
}