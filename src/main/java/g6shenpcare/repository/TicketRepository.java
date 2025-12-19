package g6shenpcare.repository;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.entity.Ticket;
import g6shenpcare.models.enums.EPriority;
import g6shenpcare.models.enums.ETicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(ETicketStatus status);
    List<Ticket> findByCustomer(UserAccount customer);
    List<Ticket> findByAssignedTo(UserAccount assignedTo);
    List<Ticket> findByCreatedBy(UserAccount createdBy);

    @Query("""
    SELECT t FROM Ticket t
    WHERE t.status IN (
        g6shenpcare.models.enums.ETicketStatus.OPEN,
        g6shenpcare.models.enums.ETicketStatus.IN_PROGRESS
    )
    ORDER BY t.priority DESC, t.createdAt ASC
""")
    List<Ticket> findOpenTickets();


    @Query("""
    SELECT t FROM Ticket t
    WHERE t.status = 'OPEN'
      AND (t.createdBy.userId = :staffId
           OR t.assignedTo.userId = :staffId)
""")
    List<Ticket> findOpenTicketsForStaff(@Param("staffId") Integer staffId);




    List<Ticket> findByPriority(EPriority priority);


    @Query("SELECT t FROM Ticket t WHERE t.assignedTo IS NULL AND t.status = 'OPEN' ORDER BY t.priority DESC, t.createdAt DESC")
    List<Ticket> findUnassignedTickets();

    List<Ticket> findByCustomerAndStatus(UserAccount customer, ETicketStatus status);


    List<Ticket> findByAssignedToAndStatus(UserAccount assignedTo, ETicketStatus status);


    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.assignedTo.userId = :userId AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveTicketsByUser(@Param("userId") Integer userId);


    @Query("SELECT t FROM Ticket t WHERE t.session.id = :sessionId ORDER BY t.createdAt DESC")
    List<Ticket> findBySessionId(@Param("sessionId") Long sessionId);


    @Query("SELECT t FROM Ticket t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:assignedToId IS NULL OR t.assignedTo.userId = :assignedToId) " +
            "ORDER BY t.priority DESC, t.createdAt DESC")
    List<Ticket> findTicketsWithFilters(
            @Param("status") ETicketStatus status,
            @Param("priority") EPriority priority,
            @Param("assignedToId") Integer assignedToId
    );

    @Query("""
    SELECT t FROM Ticket t
    WHERE t.status = 'OPEN'
      AND t.createdBy.userId = :staffId
""")
    List<Ticket> findOpenTicketsCreatedBy(@Param("staffId") Integer staffId);


    @Query("""
    SELECT t FROM Ticket t
    WHERE t.status = 'OPEN'
      AND t.assignedTo.userId = :staffId
      AND t.createdBy.userId <> :staffId
""")
    List<Ticket> findOpenTicketsAssignedTo(@Param("staffId") Integer staffId);


    List<Ticket> findByCustomer_UserIdOrderByCreatedAtDesc(Integer customer_userId);




}
