package g6shenpcare.repository;

import g6shenpcare.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> { // Sửa Long -> Integer

    Optional<UserAccount> findByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    long countByRoleIgnoreCase(String role);

    // 1. Tìm kiếm NHÂN VIÊN
    @Query("SELECT u FROM UserAccount u WHERE " +
           "(u.role <> 'CUSTOMER') AND " +
           "(:role = 'ALL' OR u.role = :role) AND " +
           "(:status = 'ALL' OR (:status = 'ACTIVE' AND u.active = true) OR (:status = 'LOCKED' AND u.active = false)) AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserAccount> searchStaff(@Param("role") String role,
                                  @Param("status") String status,
                                  @Param("keyword") String keyword);

    // 2. Tìm kiếm KHÁCH HÀNG
    @Query("SELECT u FROM UserAccount u WHERE " +
           "(u.role = 'CUSTOMER') AND " +
           "(:status = 'ALL' OR (:status = 'ACTIVE' AND u.active = true) OR (:status = 'LOCKED' AND u.active = false)) AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserAccount> searchCustomers(@Param("status") String status,
                                      @Param("keyword") String keyword);

    // Check trùng lặp
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);

    // Sửa Long -> Integer cho userId
    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Integer userId);
    boolean existsByPhoneAndUserIdNot(String phone, Integer userId);


    List<UserAccount> findByRoleAndActive(String role, Boolean isActive);

    List<UserAccount> findByRole(String role);

    List<UserAccount> findByRoleIn(List<String> roles);


    @Query("SELECT DISTINCT u.role FROM UserAccount u WHERE u.active = true")
    List<String> findDistinctActiveRoles();

}