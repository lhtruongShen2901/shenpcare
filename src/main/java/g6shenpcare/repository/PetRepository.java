package g6shenpcare.repository;

import g6shenpcare.entity.Pets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pets, Integer> { // Integer ID


    // Check trùng code
    boolean existsByPetCode(String petCode);

    // Find Pet By Owner (User)
    List<Pets> findByOwnerId(Integer ownerId);


    Optional<Pets> findByPetCode(String petCode);
    List<Pets> findByNameContainingIgnoreCase(String name);
    List<Pets> findByCustomerId(Integer customerId);
}