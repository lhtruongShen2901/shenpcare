package g6shenpcare.repository;

import g6shenpcare.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    
    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(String status);
    
    // [FIX] Tham số Integer staffId
    @Query("SELECT COUNT(l) > 0 FROM LeaveRequest l " +
           "WHERE l.staffId = :staffId " +
           "AND l.status = 'APPROVED' " +
           "AND :date BETWEEN l.fromDate AND l.toDate")
    boolean isStaffOnLeave(Integer staffId, LocalDate date);

    // [FIX] Tham số Integer staffId
    @Query("SELECT l FROM LeaveRequest l " +
           "WHERE l.staffId = :staffId " +
           "AND l.status = 'APPROVED' " +
           "AND (MONTH(l.fromDate) = :month AND YEAR(l.fromDate) = :year)")
    List<LeaveRequest> findApprovedRequestsInMonth(Integer staffId, int month, int year);
}