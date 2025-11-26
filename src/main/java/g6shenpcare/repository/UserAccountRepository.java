package g6shenpcare.repository;

import g6shenpcare.entity.UserAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Tìm user theo username (dùng cho login)
    Optional<UserAccount> findByUsername(String username);

    // Kiểm tra tồn tại username (không phân biệt hoa thường) – dùng cho đăng ký
    boolean existsByUsernameIgnoreCase(String username);

    // Đếm số user theo role (không phân biệt hoa thường) – dùng để kiểm tra có ADMIN hay chưa
    long countByRoleIgnoreCase(String role);
        // Lấy tất cả staff (role != CUSTOMER)
    List<UserAccount> findByRoleNot(String role);

    // Staff đang active
    List<UserAccount> findByRoleNotAndActiveTrue(String role);

    // Staff đang bị khóa
    List<UserAccount> findByRoleNotAndActiveFalse(String role);
}
