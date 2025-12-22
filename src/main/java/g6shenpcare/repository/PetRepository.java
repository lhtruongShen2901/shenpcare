package g6shenpcare.repository;

import g6shenpcare.entity.Pets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pets, Integer> { // ID của Pets là Integer

    // Tìm danh sách thú cưng của một khách hàng (Dùng cho form đặt lịch)
    List<Pets> findByCustomerId(Integer customerId);

    // Tìm thú cưng theo OwnerId (Nếu logic của bạn tách biệt Owner và Customer)
    List<Pets> findByOwnerId(Integer ownerId);
        
    // Tìm Pet theo Mã Code (Dùng cho tính năng "Claim" hoặc tra cứu nhanh)
    Optional<Pets> findByPetCode(String petCode);
    
    // Kiểm tra mã thú cưng đã tồn tại chưa (Tránh trùng lặp khi tạo mới)
    boolean existsByPetCode(String petCode);
    
}