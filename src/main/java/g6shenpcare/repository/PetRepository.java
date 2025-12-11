package g6shenpcare.repository;

import g6shenpcare.entity.Pets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pets, Integer> { // Integer ID
    List<Pets> findByCustomerId(Integer customerId);
    
    // Tìm Pet theo Code để Claim
    Optional<Pets> findByPetCode(String petCode);
    
    // Check trùng code
    boolean existsByPetCode(String petCode);
}