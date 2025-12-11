package g6shenpcare.controller.admin;

import g6shenpcare.dto.ScheduleAssignmentForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.StaffWorkingSchedule;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.service.LeaveService;
import g6shenpcare.service.ScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/schedule")
public class AdminScheduleController {

    private final ScheduleService scheduleService;
    private final LeaveService leaveService;
    private final StaffProfileRepository profileRepo;

    public AdminScheduleController(ScheduleService scheduleService, 
                                   LeaveService leaveService, 
                                   StaffProfileRepository profileRepo) {
        this.scheduleService = scheduleService;
        this.leaveService = leaveService;
        this.profileRepo = profileRepo;
    }

    private void addCommonHeader(Model model, Principal principal, String activeMenu) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", activeMenu);
    }

    @GetMapping("/assignment")
    public String redirectOldLink() {
        return "redirect:/admin/schedule/view";
    }

    // --- TRANG LỊCH LÀM VIỆC ---
    @GetMapping("/view")
    public String viewScheduleGrid(@RequestParam(name = "weekOffset", defaultValue = "0") int weekOffset,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model, Principal principal) {

        addCommonHeader(model, principal, "schedule-view");
        model.addAttribute("pageTitle", "Lịch Làm Việc");

        LocalDate today = LocalDate.now();
        LocalDate monday = today.plusWeeks(weekOffset).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        model.addAttribute("mondayDate", monday);
        model.addAttribute("weekOffset", weekOffset);
        model.addAttribute("keyword", keyword);

        // Lấy Lịch
        List<StaffWorkingSchedule> schedules = scheduleService.getSchedulesByDateRange(monday, sunday);
        
        // Filter theo từ khóa (nếu có)
        if (!keyword.isEmpty()) {
            String k = keyword.toLowerCase();
            schedules = schedules.stream()
                    .filter(s -> s.getStaff() != null && s.getStaff().getFullName().toLowerCase().contains(k))
                    .collect(Collectors.toList());
        }

        // Map DTO để hiển thị lên View Grid
        Map<String, List<Map<String, Object>>> calendarMap = new HashMap<>();
        for (int i = 0; i < 7; i++) {
            calendarMap.put(monday.plusDays(i).toString(), new ArrayList<>());
        }
        
        for (StaffWorkingSchedule s : schedules) {
            if (s.getWorkDate() != null) {
                String key = s.getWorkDate().toString();
                if (calendarMap.containsKey(key)) {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("scheduleId", s.getScheduleId()); // Integer
                    dto.put("startTime", s.getStartTime().toString());
                    dto.put("endTime", s.getEndTime().toString());
                    
                    Map<String, Object> staffDto = new HashMap<>();
                    if (s.getStaff() != null) {
                        staffDto.put("fullName", s.getStaff().getFullName());
                        staffDto.put("role", s.getStaff().getRole());
                    }
                    dto.put("staff", staffDto);
                    calendarMap.get(key).add(dto);
                }
            }
        }
        model.addAttribute("calendarMap", calendarMap);

        // Load danh sách đơn PENDING với thông tin chi tiết (DTO)
        model.addAttribute("pendingLeaves", leaveService.getLeaveRequestsDetailed("PENDING"));

        return "admin/schedule-view";
    }

    // --- TRANG QUẢN LÝ NGHỈ PHÉP & QUOTA (MỚI) ---
    @GetMapping("/leave-management")
    public String viewLeaveManagement(Model model, Principal principal) {
        addCommonHeader(model, principal, "leave-management");
        model.addAttribute("pageTitle", "Quản Lý Quỹ Phép");

        // 1. Lịch sử đơn từ
        model.addAttribute("leaveHistory", leaveService.getLeaveRequestsDetailed("ALL"));

        // 2. Danh sách nhân viên & Quota
        List<UserAccount> allStaff = scheduleService.getStaffForAssignment("ALL");
        List<Map<String, Object>> staffQuotaList = new ArrayList<>();
        
        for (UserAccount u : allStaff) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", u.getUserId()); // Integer
            map.put("fullName", u.getFullName());
            map.put("role", u.getRole());
            
            // Tìm Profile theo ID Integer
            StaffProfile p = profileRepo.findById(u.getUserId()).orElse(null);
            map.put("quota", (p != null) ? p.getAnnualLeaveQuota() : 0);
            
            staffQuotaList.add(map);
        }
        model.addAttribute("staffQuotaList", staffQuotaList);

        // 3. Danh sách chính sách
        model.addAttribute("policies", leaveService.getAllPolicies());

        return "admin/leave-management";
    }

    // --- API QUOTA & POLICY ---
    @PostMapping("/policy/update")
    public String updatePolicy(@RequestParam("role") String role, 
                               @RequestParam("maxMonth") int max, 
                               @RequestParam("defaultYear") int def, 
                               RedirectAttributes ra) {
        leaveService.updatePolicy(role, max, def);
        ra.addFlashAttribute("message", "Đã cập nhật chính sách.");
        return "redirect:/admin/schedule/leave-management";
    }

    @PostMapping("/quota/update")
    public String updateQuota(@RequestParam("staffId") Integer id, // Integer
                              @RequestParam("quota") Integer q, 
                              RedirectAttributes ra) {
        leaveService.updateStaffQuota(id, q); 
        ra.addFlashAttribute("message", "Đã cập nhật quỹ phép.");
        return "redirect:/admin/schedule/leave-management";
    }

    @PostMapping("/quota/update-bulk")
    public String updateQuotaBulk(@RequestParam("role") String r, 
                                  @RequestParam("quota") Integer q, 
                                  RedirectAttributes ra) {
        leaveService.updateQuotaByRole(r, q);
        ra.addFlashAttribute("message", "Đã cập nhật hàng loạt.");
        return "redirect:/admin/schedule/leave-management";
    }

    @PostMapping("/quota/reset-year")
    public String resetAnnualQuotaManually(RedirectAttributes ra) {
        leaveService.resetAnnualLeaveQuota();
        ra.addFlashAttribute("message", "Đã RESET toàn bộ phép năm!");
        return "redirect:/admin/schedule/leave-management";
    }

    // --- API JSON ---
    @GetMapping("/api/staff-detail")
    @ResponseBody
    public Map<String, Object> getStaffDetail(@RequestParam("id") Integer staffId) { // Integer
        Map<String, Object> response = new HashMap<>();
        UserAccount user = scheduleService.getStaffById(staffId);
        
        if (user != null) {
            response.put("fullName", user.getFullName());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            
            StaffProfile profile = profileRepo.findById(staffId).orElse(null);
            if (profile != null) {
                response.put("specialization", profile.getSpecialization());
                response.put("quotaTotal", profile.getAnnualLeaveQuota());
            } else {
                response.put("quotaTotal", 0);
            }
        }
        return response;
    }

    @GetMapping("/api/staff-by-role")
    @ResponseBody
    public List<Map<String, Object>> getStaffByRole(@RequestParam("role") String role) {
        List<UserAccount> staffList = scheduleService.getStaffForAssignment(role);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (UserAccount u : staffList) {
            Map<String, Object> m = new HashMap<>();
            m.put("userId", u.getUserId()); // Integer
            m.put("fullName", u.getFullName());
            result.add(m);
        }
        return result;
    }

    // --- ACTION HANDLERS ---
    @PostMapping("/leave/create")
    public String createLeave(@RequestParam("staffId") Integer staffId, // Integer
            @RequestParam(value = "creatorId", required = false) Integer creatorId, // Integer
            @RequestParam("fromDate") LocalDate from,
            @RequestParam("toDate") LocalDate to,
            @RequestParam("reason") String reason,
            RedirectAttributes ra) {
        try {
            int finalCreatorId = (creatorId != null) ? creatorId : 1;
            leaveService.createRequest(staffId, finalCreatorId, from, to, reason);
            ra.addFlashAttribute("message", "Đã tạo đơn thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/schedule/view";
    }

    @PostMapping("/leave/approve")
    public String approveLeave(@RequestParam("requestId") Integer id, // Integer
                               @RequestParam("leaveType") String type, 
                               @RequestParam(required = false) String note, 
                               RedirectAttributes ra) {
        leaveService.approveRequest(id, type, note);
        ra.addFlashAttribute("message", "Đã duyệt đơn.");
        return "redirect:/admin/schedule/view";
    }

    @PostMapping("/leave/reject")
    public String rejectLeave(@RequestParam("requestId") Integer id, // Integer
                              @RequestParam("note") String note, 
                              RedirectAttributes ra) {
        leaveService.rejectRequest(id, note);
        ra.addFlashAttribute("message", "Đã từ chối đơn.");
        return "redirect:/admin/schedule/view";
    }

    @PostMapping("/assignment/save")
    public String saveAssignment(@ModelAttribute ScheduleAssignmentForm form, RedirectAttributes ra) {
        try {
            // Form đã được cập nhật để chứa List<Integer>
            List<String> warnings = scheduleService.assignSchedule(form);
            
            if (warnings.isEmpty()) {
                ra.addFlashAttribute("message", "Đã phân công thành công!");
            } else {
                ra.addFlashAttribute("message", "Đã lưu các ca hợp lệ.");
                ra.addFlashAttribute("error", "Lưu ý: Có " + warnings.size() + " ca bị bỏ qua.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/schedule/view";
    }

    @PostMapping("/delete-slot")
    public String deleteSlot(@RequestParam("id") Integer id, // Integer
                             @RequestParam(name = "weekOffset", defaultValue = "0") int w, 
                             RedirectAttributes ra) {
        scheduleService.deleteSchedule(id);
        ra.addFlashAttribute("message", "Đã xóa ca.");
        return "redirect:/admin/schedule/view?weekOffset=" + w;
    }

    @PostMapping("/update-slot")
    public String updateSlot(@RequestParam("id") Integer id, // Integer
                             @RequestParam("startTime") LocalTime s, 
                             @RequestParam("endTime") LocalTime e, 
                             @RequestParam(name = "weekOffset", defaultValue = "0") int w, 
                             RedirectAttributes ra) {
        scheduleService.updateSingleSchedule(id, s, e);
        ra.addFlashAttribute("message", "Đã cập nhật giờ.");
        return "redirect:/admin/schedule/view?weekOffset=" + w;
    }
}