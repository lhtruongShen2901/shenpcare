package g6shenpcare.repository;

import g6shenpcare.models.entity.PetMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetMedicalRecordRepository extends JpaRepository<PetMedicalRecord, Integer> {
    List<PetMedicalRecord> findByPet_PetIdOrderByVisitDateDesc(Integer petId);
    Optional<PetMedicalRecord> findByBooking_BookingId(Integer bookingId);

    Optional<PetMedicalRecord> findTopByPet_PetIdOrderByVisitDateDesc(Integer petId);


}