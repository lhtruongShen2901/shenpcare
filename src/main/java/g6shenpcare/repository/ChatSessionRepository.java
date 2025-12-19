package g6shenpcare.repository;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.enums.ESessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByStatus(ESessionStatus status);
    List<ChatSession> findBySupportStaffAndStatus(UserAccount supportStaff, ESessionStatus status);
    List<ChatSession> findByCustomer(UserAccount customer);

    @Query("SELECT s FROM ChatSession s WHERE s.customer = ?1 AND s.status != 'CLOSED' ORDER BY s.startedAt DESC")
    Optional<ChatSession> findActiveSessionByCustomer(UserAccount customer);

    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.supportStaff = ?1 AND s.status = 'ACTIVE'")
    Long countActiveSessionsByStaff(UserAccount supportStaff);


    Optional<ChatSession> findBySupportStaff_UserIdAndCustomer_UserIdAndStatus(
            Long supportStaffId,
            Long customerId,
            ESessionStatus status);

}
