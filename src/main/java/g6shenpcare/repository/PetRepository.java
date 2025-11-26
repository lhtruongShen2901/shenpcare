package g6shenpcare.repository;

import g6shenpcare.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

    long countByIsActiveTrue();

}
