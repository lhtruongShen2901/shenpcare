package g6shenpcare.repository;

import g6shenpcare.entity.PrescriptionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionDetail, Long> {
    // Hiện tại chúng ta chỉ cần các hàm cơ bản (save, delete) có sẵn của JPA
    // Sau này nếu muốn tìm đơn thuốc của bệnh nhân nào thì viết thêm query sau
}