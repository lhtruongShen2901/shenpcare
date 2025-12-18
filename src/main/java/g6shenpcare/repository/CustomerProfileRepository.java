package g6shenpcare.repository;

import g6shenpcare.entity.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

    long countByIsActiveTrue();
    Optional<CustomerProfile> findByUserId(Integer userId); // Lưu ý: userId trong UserAccount là Integer
}
