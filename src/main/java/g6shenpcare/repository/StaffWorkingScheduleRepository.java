package g6shenpcare.repository;

import g6shenpcare.entity.StaffWorkingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffWorkingScheduleRepository extends JpaRepository<StaffWorkingSchedule, Integer> {
    
    // 1. Lấy lịch trong khoảng ngày (cho View Grid)
    List<StaffWorkingSchedule> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    // 2. Tìm lịch của nhân viên trong 1 ngày cụ thể (Check trùng bằng Java)
    List<StaffWorkingSchedule> findByStaffIdAndWorkDate(Integer staffId, LocalDate workDate);
    
    // 3. Xóa lịch (Dùng khi duyệt đơn nghỉ phép)
    void deleteByStaffIdAndWorkDate(Integer staffId, LocalDate workDate);
}