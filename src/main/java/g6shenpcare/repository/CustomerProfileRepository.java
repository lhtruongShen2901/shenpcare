package g6shenpcare.repository;

import g6shenpcare.entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {

    long countByIsActiveTrue();

}
