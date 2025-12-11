package g6shenpcare.service;

import g6shenpcare.dto.LeaveRequestDetailDTO;
import g6shenpcare.entity.*;
import g6shenpcare.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeaveService {

    @Autowired private LeaveRequestRepository leaveRepo;
    @Autowired private StaffProfileRepository profileRepo;
    @Autowired private StaffWorkingScheduleRepository scheduleRepo;
    @Autowired private UserAccountRepository userRepo;
    @Autowired private LeavePolicyRepository policyRepo;

    public List<LeaveRequestDetailDTO> getLeaveRequestsDetailed(String status) {
        List<LeaveRequest> requests;
        if ("ALL".equals(status)) {
            requests = leaveRepo.findAll();
        } else {
            requests = leaveRepo.findByStatusOrderByCreatedAtDesc(status);
        }

        List<LeaveRequestDetailDTO> dtos = new ArrayList<>();
        for (LeaveRequest req : requests) {
            LeaveRequestDetailDTO dto = new LeaveRequestDetailDTO();
            dto.setRequestId(req.getRequestId());
            dto.setFromDate(req.getFromDate());
            dto.setToDate(req.getToDate());
            dto.setReason(req.getReason());
            dto.setStatus(req.getStatus());
            dto.setLeaveType(req.getLeaveType());

            if (req.getStaff() != null) {
                dto.setStaffName(req.getStaff().getFullName());
                dto.setStaffRole(req.getStaff().getRole());
            }

            long days = ChronoUnit.DAYS.between(req.getFromDate(), req.getToDate()) + 1;
            dto.setDaysRequested((int) days);

            // getStaffId() trả về Integer -> khớp với findById(Integer)
            StaffProfile profile = profileRepo.findById(req.getStaffId()).orElse(null);
            if (profile != null) {
                dto.setCurrentQuota(profile.getAnnualLeaveQuota());
                if ("PENDING".equals(req.getStatus())) {
                    int excess = (int) days - profile.getAnnualLeaveQuota();
                    dto.setExcessDays(Math.max(0, excess));
                } else {
                    dto.setExcessDays(0);
                }
            }
            dtos.add(dto);
        }
        return dtos;
    }

    public List<LeaveRequest> getPendingRequests() {
        return leaveRepo.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    // Tham số staffId là Integer
    public void createRequest(Integer staffId, Integer creatorId, LocalDate from, LocalDate to, String reason) {
        if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");

        UserAccount user = userRepo.findById(staffId).orElseThrow();
        LeavePolicy policy = policyRepo.findByRoleName(user.getRole()).orElse(null);

        if (policy != null) {
            long requestingDays = ChronoUnit.DAYS.between(from, to) + 1;
            int currentMonth = from.getMonthValue();
            int currentYear = from.getYear();
            
            // staffId là Integer -> truyền trực tiếp
            List<LeaveRequest> history = leaveRepo.findApprovedRequestsInMonth(staffId, currentMonth, currentYear);
            
            int usedDaysInMonth = history.stream()
                .mapToInt(r -> (int) ChronoUnit.DAYS.between(r.getFromDate(), r.getToDate()) + 1)
                .sum();

            if (usedDaysInMonth + requestingDays > policy.getMaxDaysPerMonth()) {
                throw new IllegalArgumentException("Vượt quá giới hạn! " + user.getRole() + " chỉ được nghỉ " + policy.getMaxDaysPerMonth() + " ngày/tháng.");
            }
        }

        LeaveRequest req = new LeaveRequest();
        req.setStaffId(staffId); // Integer
        req.setCreatedByStaffId(creatorId);
        req.setFromDate(from);
        req.setToDate(to);
        req.setReason(reason);
        req.setStatus("PENDING");
        leaveRepo.save(req);
    }

    @Transactional
    public void approveRequest(Integer requestId, String leaveType, String note) {
        LeaveRequest req = leaveRepo.findById(requestId).orElseThrow();
        req.setStatus("APPROVED");
        req.setLeaveType(leaveType);
        req.setAdminNote(note);
        req.setApprovedAt(LocalDateTime.now());
        leaveRepo.save(req);

        if ("ANNUAL".equals(leaveType)) {
            // req.getStaffId() là Integer
            StaffProfile profile = profileRepo.findById(req.getStaffId()).orElseThrow();
            long days = ChronoUnit.DAYS.between(req.getFromDate(), req.getToDate()) + 1;
            profile.setAnnualLeaveQuota(profile.getAnnualLeaveQuota() - (int) days);
            profileRepo.save(profile);
        }

        LocalDate current = req.getFromDate();
        while (!current.isAfter(req.getToDate())) {
            // [FIXED] Xóa bỏ .longValue(). Truyền trực tiếp Integer.
            scheduleRepo.deleteByStaffIdAndWorkDate(req.getStaffId(), current);
            current = current.plusDays(1);
        }
    }

    @Transactional
    public void rejectRequest(Integer requestId, String note) {
        LeaveRequest req = leaveRepo.findById(requestId).orElseThrow();
        req.setStatus("REJECTED");
        req.setAdminNote(note);
        leaveRepo.save(req);
    }

    @Scheduled(cron = "0 0 0 1 1 ?") 
    @Transactional
    public void resetAnnualLeaveQuota() {
        List<UserAccount> allStaff = userRepo.findAll();
        for (UserAccount user : allStaff) {
            if (!"CUSTOMER".equals(user.getRole())) {
                StaffProfile profile = profileRepo.findById(user.getUserId()).orElse(null);
                LeavePolicy policy = policyRepo.findByRoleName(user.getRole()).orElse(null);
                
                if (profile != null && policy != null) {
                    profile.setAnnualLeaveQuota(policy.getDefaultAnnualQuota());
                    profileRepo.save(profile);
                }
            }
        }
    }

    public List<LeavePolicy> getAllPolicies() { return policyRepo.findAll(); }

    @Transactional
    public void updatePolicy(String role, int maxMonth, int defaultYear) {
        LeavePolicy p = policyRepo.findByRoleName(role).orElse(new LeavePolicy());
        p.setRoleName(role);
        p.setMaxDaysPerMonth(maxMonth);
        p.setDefaultAnnualQuota(defaultYear);
        policyRepo.save(p);
    }

    @Transactional
    public void updateStaffQuota(Integer staffId, int newQuota) {
        StaffProfile profile = profileRepo.findById(staffId).orElseThrow();
        profile.setAnnualLeaveQuota(newQuota);
        profileRepo.save(profile);
    }
    
    @Transactional
    public void updateQuotaByRole(String role, int newQuota) {
        List<UserAccount> users = userRepo.searchStaff(role, "ACTIVE", "");
        for (UserAccount u : users) {
            StaffProfile p = profileRepo.findById(u.getUserId()).orElse(null);
            if (p != null) {
                p.setAnnualLeaveQuota(newQuota);
                profileRepo.save(p);
            }
        }
    }
    
    // staffId là Integer
    public boolean isStaffOnLeave(Integer staffId, LocalDate date) {
        return leaveRepo.isStaffOnLeave(staffId, date);
    }
}