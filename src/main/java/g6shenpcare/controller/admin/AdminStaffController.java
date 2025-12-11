package g6shenpcare.controller.admin;

import g6shenpcare.dto.UserForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/staff")
public class AdminStaffController {

    private final UserService userService;
    private final StaffProfileRepository staffProfileRepository;

    public AdminStaffController(UserService userService, StaffProfileRepository staffProfileRepository) {
        this.userService = userService;
        this.staffProfileRepository = staffProfileRepository;
    }

    // --- Helper Methods ---
    private List<String> getStaffRoles() {
        return Arrays.asList("ADMIN", "DOCTOR", "GROOMER", "SUPPORT", "STORE", "ACCOUNTANT");
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "staff");
    }

    // 1. DANH SÁCH NHÂN VIÊN
    @GetMapping
    public String listStaff(@RequestParam(name = "role", defaultValue = "ALL") String role,
            @RequestParam(name = "status", defaultValue = "ALL") String status,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            Model model, Principal principal) {

        List<UserAccount> staffList = userService.searchStaff(role, status, keyword);
        List<StaffProfile> profiles = staffProfileRepository.findAll();

        // [FIX] Map Key là Integer
        Map<Integer, StaffProfile> profileMap = profiles.stream()
                .collect(Collectors.toMap(StaffProfile::getStaffId, p -> p));

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Nhân sự");
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", Arrays.asList("ALL", "ACTIVE", "LOCKED"));
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        model.addAttribute("users", staffList);
        model.addAttribute("profileMap", profileMap);
        model.addAttribute("totalUsers", staffList.size());
        model.addAttribute("displayedCount", staffList.size());

        return "admin/staff-list";
    }

    // --- API 1: LẤY CHI TIẾT NHÂN VIÊN ---
    @GetMapping("/{id}/detail-api")
    @ResponseBody
    public Map<String, Object> getStaffDetail(@PathVariable("id") Integer id) { // [FIX] Integer
        UserAccount user = userService.getUserById(id);
        StaffProfile profile = staffProfileRepository.findById(id).orElse(null);

        Map<String, Object> data = new HashMap<>();
        data.put("fullName", user.getFullName());
        data.put("username", user.getUsername());
        data.put("email", user.getEmail());
        data.put("phone", user.getPhone());
        data.put("role", user.getRole());
        data.put("active", user.isActive());

        if (profile != null) {
            data.put("staffCode", profile.getStaffCode());
            data.put("hireDate", profile.getHireDate());
            data.put("specialization", profile.getSpecialization());
            data.put("license", profile.getLicenseNumber());
        } else {
            data.put("staffCode", "N/A");
            data.put("hireDate", null);
            data.put("specialization", "Chưa cập nhật");
            data.put("license", "N/A");
        }
        return data;
    }

    // --- API 2: LẤY DANH SÁCH THAY THẾ ---
    @GetMapping("/{id}/replacements")
    @ResponseBody
    public List<Map<String, Object>> getReplacements(@PathVariable("id") Integer lockedId) { // [FIX] Integer
        UserAccount lockedUser = userService.getUserById(lockedId);
        List<UserAccount> candidates = userService.searchStaff(lockedUser.getRole(), "ACTIVE", "");

        List<Map<String, Object>> result = new ArrayList<>();
        for (UserAccount u : candidates) {
            if (!Objects.equals(u.getUserId(), lockedId)) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", u.getUserId());
                map.put("fullName", u.getFullName() + " (" + u.getUsername() + ")");
                result.add(map);
            }
        }
        return result;
    }

    // 2. FORM THÊM MỚI
    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        UserForm form = new UserForm();
        form.setStatus("ACTIVE");
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Thêm Nhân viên mới");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", Arrays.asList("ACTIVE", "LOCKED"));
        model.addAttribute("isEdit", false);
        return "admin/staff-detail";
    }

    // 3. FORM CHỈNH SỬA
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") Integer userId, Model model, Principal principal) { // [FIX] Integer
        UserAccount user = userService.getUserById(userId);
        UserForm form = new UserForm();
        
        // [FIX QUAN TRỌNG] Bỏ .longValue(), gán trực tiếp Integer -> Integer
        form.setUserId(user.getUserId()); 
        
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setStatus(user.isActive() ? "ACTIVE" : "LOCKED");

        // Repository đã sửa thành Integer ID, nên findById(userId) hoạt động tốt
        staffProfileRepository.findById(userId).ifPresent(sp -> {
            form.setHireDate(sp.getHireDate());
            form.setSpecialization(sp.getSpecialization());
            form.setLicenseNumber(sp.getLicenseNumber());
            form.setStaffCode(sp.getStaffCode());
        });

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Chỉnh sửa Nhân viên");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", Arrays.asList("ACTIVE", "LOCKED"));
        model.addAttribute("isEdit", true);
        return "admin/staff-detail";
    }

    // 4. LƯU DỮ LIỆU
    @PostMapping("/save")
    public String saveStaff(@ModelAttribute("userForm") UserForm form, Model model, RedirectAttributes ra) {
        try {
            userService.saveUser(form);
            ra.addFlashAttribute("message", (form.getUserId() != null) ? "Đã cập nhật nhân viên." : "Đã thêm nhân viên mới.");
            return "redirect:/admin/staff";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            addCommonHeader(model, null);
            model.addAttribute("pageTitle", (form.getUserId() != null) ? "Chỉnh sửa Nhân viên" : "Thêm Nhân viên mới");
            model.addAttribute("roles", getStaffRoles());
            model.addAttribute("statuses", Arrays.asList("ACTIVE", "LOCKED"));
            model.addAttribute("isEdit", (form.getUserId() != null));
            model.addAttribute("userForm", form);
            return "admin/staff-detail";
        }
    }

    // 5. KHÓA TÀI KHOẢN (ĐƠN GIẢN)
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Integer userId, Principal principal, RedirectAttributes ra) { // [FIX] Integer
        try {
            userService.toggleUserStatus(userId, principal.getName());
            ra.addFlashAttribute("message", "Đã cập nhật trạng thái tài khoản.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    // 6. KHÓA TÀI KHOẢN KÈM BÀN GIAO (NÂNG CAO)
    @PostMapping("/deactivate")
    public String deactivateStaff(@RequestParam("id") Integer staffId, // [FIX] Integer
                                  @RequestParam(value = "replacementId", required = false) Integer replacementId, // [FIX] Integer
                                  Principal principal,
                                  RedirectAttributes ra) {
        try {
            userService.deactivateStaffSafe(staffId, replacementId, principal.getName());
            ra.addFlashAttribute("message", "Đã khóa nhân viên và bàn giao công việc thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/staff";
    }

    // 7. XÓA NHÂN VIÊN
    @PostMapping("/delete")
    public String deleteStaff(@RequestParam("id") Integer id, RedirectAttributes ra) { // [FIX] Integer
        try {
            userService.deleteUser(id); 
            ra.addFlashAttribute("message", "Đã xóa nhân viên thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }
}