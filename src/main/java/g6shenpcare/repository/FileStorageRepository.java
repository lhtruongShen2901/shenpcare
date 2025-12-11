package g6shenpcare.repository;

import g6shenpcare.entity.FileStorage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileStorageRepository extends JpaRepository<FileStorage, Integer> {
    // Hiện tại chưa cần query phức tạp, chỉ cần save/findById mặc định là đủ
}