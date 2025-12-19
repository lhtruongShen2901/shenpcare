package g6shenpcare.repository;

import g6shenpcare.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

    long countByIsActiveTrue();
    List<CustomerProfile> findByEmailContainingIgnoreCase(String email);
    List<CustomerProfile> findByPhoneContaining(String phone);


}
