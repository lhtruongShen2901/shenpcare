package g6shenpcare.repository;

import g6shenpcare.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Đếm số lượng booking theo trạng thái (Dùng cho Dashboard)
    long countByStatusIgnoreCase(String status);
    
    // Kiểm tra Pet có booking nào không (Dùng khi xóa Pet)
    boolean existsByPetId(Integer petId);

    // --- CÁC HÀM MỚI CHO LOGIC KHÓA NHÂN VIÊN ---

    // 1. Đếm số lịch hẹn chưa hoàn thành của nhân viên
    // Lưu ý: Trường b.assignedStaffId phải khớp với tên biến trong Entity Booking
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.assignedStaffId = :staffId " +
           "AND b.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')")
    long countActiveBookingsByStaff(@Param("staffId") Integer staffId);

    // 2. Chuyển lịch sang nhân viên mới
    @Modifying
    @Query("UPDATE Booking b SET b.assignedStaffId = :newStaffId " +
           "WHERE b.assignedStaffId = :oldStaffId " +
           "AND b.status IN ('PENDING', 'CONFIRMED')") // Chỉ chuyển lịch chưa diễn ra
    void reassignBookings(@Param("oldStaffId") Integer oldStaffId, 
                          @Param("newStaffId") Integer newStaffId);
}