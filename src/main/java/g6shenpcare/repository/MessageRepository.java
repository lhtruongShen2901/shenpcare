package g6shenpcare.repository;


import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.entity.ChatSession;
import g6shenpcare.models.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySessionOrderBySentAtAsc(ChatSession session);

    @Query("SELECT m FROM Message m WHERE m.session = ?1 AND m.isRead = false")
    List<Message> findUnreadMessagesBySession(ChatSession session);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.session.supportStaff = ?1 AND m.isRead = false AND m.sender.role = 'CUSTOMER'")
    Long countUnreadMessagesForStaff(UserAccount staff);

    @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.session = :session
          AND m.isRead = false
          AND m.sender.userId <> :staffId
    """)
    long countUnreadMessages(ChatSession session, int staffId);

    Message findTopBySessionOrderBySentAtDesc(ChatSession session);
}
