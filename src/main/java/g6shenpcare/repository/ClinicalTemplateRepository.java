package g6shenpcare.repository;

import g6shenpcare.entity.ClinicalTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClinicalTemplateRepository extends JpaRepository<ClinicalTemplate, Integer> {
    // Tìm các mẫu đang hoạt động (IsActive = true)
    List<ClinicalTemplate> findByIsActiveTrue();
}