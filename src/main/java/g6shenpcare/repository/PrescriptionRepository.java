package g6shenpcare.repository;


import g6shenpcare.models.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    List<Prescription> findByRecord_RecordId(Integer recordId);

    @Query("""
        SELECT p
        FROM Prescription p
        WHERE p.record.pet.petId = :petId
    """)
    List<Prescription> findByPetId(@Param("petId") Integer petId);

}

