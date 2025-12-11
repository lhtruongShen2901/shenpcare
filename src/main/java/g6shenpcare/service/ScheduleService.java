package g6shenpcare.service;

import g6shenpcare.dto.ScheduleAssignmentForm;
import g6shenpcare.entity.StaffWorkingSchedule;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.LeaveRequestRepository;
import g6shenpcare.repository.StaffWorkingScheduleRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private final StaffWorkingScheduleRepository scheduleRepo;
    private final UserAccountRepository userRepo;
    private final LeaveRequestRepository leaveRepo;

    public ScheduleService(StaffWorkingScheduleRepository scheduleRepo,
            UserAccountRepository userRepo,
            LeaveRequestRepository leaveRepo) {
        this.scheduleRepo = scheduleRepo;
        this.userRepo = userRepo;
        this.leaveRepo = leaveRepo;
    }

    @Transactional
    public List<String> assignSchedule(ScheduleAssignmentForm form) {
        List<String> warnings = new ArrayList<>();
        List<StaffWorkingSchedule> schedulesToSave = new ArrayList<>();

        if (form.getStaffIds() == null || form.getStaffIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một nhân viên để phân công!");
        }

        LocalTime start = (form.getCustomStartTime() != null) ? form.getCustomStartTime() : LocalTime.of(8, 0);
        LocalTime end = (form.getCustomEndTime() != null) ? form.getCustomEndTime() : LocalTime.of(17, 0);

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Giờ kết thúc sai (phải sau giờ bắt đầu)!");
        }

        // staffId là Integer
        for (Integer staffId : form.getStaffIds()) {
            LocalDate currentDate = form.getFromDate();

            while (!currentDate.isAfter(form.getToDate())) {
                int dayOfWeek = currentDate.getDayOfWeek().getValue();

                if (form.getDaysOfWeek() != null && form.getDaysOfWeek().contains(dayOfWeek)) {
                    
                    // [FIX] Truyền thẳng Integer staffId (đã bỏ .longValue())
                    boolean onLeave = leaveRepo.isStaffOnLeave(staffId, currentDate);

                    if (onLeave) {
                        warnings.add("NV " + staffId + " nghỉ phép ngày " + currentDate + " (Bỏ qua).");
                    } else {
                        // [FIX] Truyền thẳng Integer staffId (đã bỏ .longValue())
                        List<StaffWorkingSchedule> existing = scheduleRepo.findByStaffIdAndWorkDate(staffId, currentDate);
                        boolean overlap = false;
                        for (StaffWorkingSchedule ex : existing) {
                            if (start.isBefore(ex.getEndTime()) && end.isAfter(ex.getStartTime())) {
                                overlap = true;
                                break;
                            }
                        }

                        if (!overlap) {
                            StaffWorkingSchedule s = new StaffWorkingSchedule();
                            // [FIX] Truyền thẳng Integer staffId (đã bỏ .longValue())
                            s.setStaffId(staffId); 
                            s.setWorkDate(currentDate);
                            s.setDayOfWeek(dayOfWeek);
                            s.setStartTime(start);
                            s.setEndTime(end);
                            s.setActive(true);
                            schedulesToSave.add(s);
                        } else {
                            warnings.add("NV " + staffId + " bị trùng giờ ngày " + currentDate);
                        }
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        if (!schedulesToSave.isEmpty()) {
            scheduleRepo.saveAll(schedulesToSave);
        }

        return warnings;
    }

    @Transactional
    public void updateSingleSchedule(Integer id, LocalTime s, LocalTime e) {
        StaffWorkingSchedule sw = scheduleRepo.findById(id).orElseThrow();
        sw.setStartTime(s);
        sw.setEndTime(e);
        scheduleRepo.save(sw);
    }

    public List<UserAccount> getStaffForAssignment(String role) {
        String searchRole = (role == null || role.trim().isEmpty()) ? "ALL" : role;
        return userRepo.searchStaff(searchRole, "ACTIVE", "");
    }

    public UserAccount getStaffById(Integer id) {
        return userRepo.findById(id).orElse(null);
    }

    public List<StaffWorkingSchedule> getSchedulesByDateRange(LocalDate start, LocalDate end) {
        return scheduleRepo.findByWorkDateBetween(start, end);
    }

    @Transactional
    public void deleteSchedule(Integer id) {
        scheduleRepo.deleteById(id);
    }
}