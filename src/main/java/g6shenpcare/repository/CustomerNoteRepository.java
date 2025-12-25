package g6shenpcare.repository;

import g6shenpcare.models.entity.CustomerNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerNoteRepository extends JpaRepository<CustomerNote, Integer> {

    List<CustomerNote> findByCustomer_CustomerIdOrderByCreatedAtDesc(Integer customerId);

    List<CustomerNote> findByCustomer_CustomerIdAndNoteType(Integer customerId, String noteType);

    List<CustomerNote> findByRelatedSessionId(Integer sessionId);
}