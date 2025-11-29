package g6shenpcare.repository;
import g6shenpcare.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Integer> {
    List<ServiceCategory> findByActiveTrue(); // Lấy danh sách đang hoạt động
}