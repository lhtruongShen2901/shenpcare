package g6shenpcare.repository;

import g6shenpcare.entity.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

    // Đếm số lượng khách hàng đang hoạt động (dùng cho thống kê)
    long countByIsActiveTrue();

    // Tìm hồ sơ khách hàng dựa trên User ID (Liên kết 1-1 giữa UserAccount và CustomerProfile)
    // Lưu ý: Trong Entity CustomerProfile cần có trường 'userId' hoặc quan hệ @OneToOne mapping tới field này.
    // Nếu bạn map quan hệ là "private UserAccount user;", hãy đổi tên hàm thành: findByUser_UserId(Integer userId)
    Optional<CustomerProfile> findByUserId(Integer userId); 
    
}