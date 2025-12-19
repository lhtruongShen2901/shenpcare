package g6shenpcare.repository;

import g6shenpcare.entity.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

    long countByIsActiveTrue();
    Optional<CustomerProfile> findByUserId(Integer userId); // Lưu ý: userId trong UserAccount là Integer
    List<CustomerProfile> findByEmailContainingIgnoreCase(String email);
    List<CustomerProfile> findByPhoneContaining(String phone);


}
