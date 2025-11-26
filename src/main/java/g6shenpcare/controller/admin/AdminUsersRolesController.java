package g6shenpcare.controller.admin;

import g6shenpcare.dto.UserForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminUsersRolesController {

    private final UserAccountRepository userAccountRepository;
    private final StaffProfileRepository staffProfileRepository;

    public AdminUsersRolesController(UserAccountRepository userAccountRepository,
                                     StaffProfileRepository staffProfileRepository) {
        this.userAccountRepository = userAccountRepository;
        this.staffProfileRepository = staffProfileRepository;
    }

    // ==== helper: danh sách role & status cho staff ====

    private List<String> getStaffRoles() {
        return Arrays.asList(
                "ADMIN",
                "DOCTOR",
                "GROOMER",
                "SUPPORT",
                "STORE",
                "ACCOUNTANT"
        );
    }

    private List<String> getStatusOptions() {
        return Arrays.asList("ACTIVE", "LOCKED");
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "users");
    }

    // ================== LIST USERS & ROLES ==================
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(name = "role", required = false, defaultValue = "ALL") String roleFilter,
            @RequestParam(name = "status", required = false, defaultValue = "ALL") String statusFilter,
            @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
            Model model,
            Principal principal
    ) {
        // Roles cho filter (staff only)
        List<String> roles = new LinkedList<>();
        roles.add("ALL");
        roles.addAll(getStaffRoles());

        List<String> statuses = Arrays.asList("ALL", "ACTIVE", "LOCKED");

        // Lấy toàn bộ user
        List<UserAccount> allUsers = userAccountRepository.findAll();

        // 1. Chỉ lấy staff (loại CUSTOMER)
        List<UserAccount> staffUsers = allUsers.stream()
                .filter(u -> u.getRole() == null
                        || !u.getRole().equalsIgnoreCase("CUSTOMER"))
                .collect(Collectors.toList());

        List<UserAccount> filteredUsers = staffUsers;

        // 2. Tìm kiếm theo keyword (username / fullName / email)
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        if (!kw.isEmpty()) {
            filteredUsers = filteredUsers.stream()
                    .filter(u ->
                            (u.getUsername() != null && u.getUsername().toLowerCase(Locale.ROOT).contains(kw)) ||
                            (u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(kw)) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase(Locale.ROOT).contains(kw))
                    )
                    .collect(Collectors.toList());
        }

        // 3. Filter theo role (nếu != ALL)
        if (!"ALL".equalsIgnoreCase(roleFilter)) {
            filteredUsers = filteredUsers.stream()
                    .filter(u -> roleFilter.equalsIgnoreCase(u.getRole()))
                    .collect(Collectors.toList());
        }

        // 4. Filter theo status (ACTIVE / LOCKED)
        if ("ACTIVE".equalsIgnoreCase(statusFilter)) {
            filteredUsers = filteredUsers.stream()
                    .filter(UserAccount::isActive)
                    .collect(Collectors.toList());
        } else if ("LOCKED".equalsIgnoreCase(statusFilter)) {
            filteredUsers = filteredUsers.stream()
                    .filter(u -> !u.isActive())
                    .collect(Collectors.toList());
        }

        long totalUsers = staffUsers.size();          // tổng staff
        long displayedCount = filteredUsers.size();   // số đang hiển thị

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Users & Roles - ShenPCare Admin");

        model.addAttribute("roles", roles);
        model.addAttribute("statuses", statuses);
        model.addAttribute("selectedRole", roleFilter);
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);

        model.addAttribute("users", filteredUsers);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("displayedCount", displayedCount);

        return "admin/users";
    }

    // ================== CREATE FORM ==================
    @GetMapping("/users/new")
    public String showCreateForm(Model model, Principal principal) {

        UserForm form = new UserForm();
        form.setStatus("ACTIVE");   // mặc định

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Thêm người dùng mới");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", getStatusOptions());
        model.addAttribute("isEdit", false);

        return "admin/user-detail";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/users/{id}")
    public String showEditForm(@PathVariable("id") Long userId,
                               Model model,
                               Principal principal) {

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserForm form = new UserForm();
        form.setUserId(user.getUserId());
        form.setUsername(user.getUsername());
        form.setFullName(user.getFullName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole());
        form.setStatus(user.isActive() ? "ACTIVE" : "LOCKED");

        // Lấy StaffProfile (nếu có) – StaffId = UserId
        staffProfileRepository.findById(userId).ifPresent(sp -> {
            form.setHireDate(sp.getHireDate());
            form.setSpecialization(sp.getSpecialization());
            form.setLicenseNumber(sp.getLicenseNumber());
        });

        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Chỉnh sửa người dùng");
        model.addAttribute("userForm", form);
        model.addAttribute("roles", getStaffRoles());
        model.addAttribute("statuses", getStatusOptions());
        model.addAttribute("isEdit", true);

        return "admin/user-detail";
    }

    // ================== SAVE (CREATE/UPDATE) ==================
    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute("userForm") UserForm form,
                           RedirectAttributes redirectAttributes) {

        boolean isEdit = (form.getUserId() != null);

        UserAccount user;
        if (isEdit) {
            user = userAccountRepository.findById(form.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + form.getUserId()));
        } else {
            user = new UserAccount();
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setUsername(form.getUsername());
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setRole(form.getRole());
        user.setActive(!"LOCKED".equalsIgnoreCase(form.getStatus()));
        user.setUpdatedAt(LocalDateTime.now());

        // TODO: sau này hãy mã hoá password bằng PasswordEncoder
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(form.getPassword().trim());
        }

        UserAccount savedUser = userAccountRepository.save(user);

        // StaffProfile: chỉ tạo/cập nhật cho user không phải CUSTOMER
        if (form.getRole() != null && !"CUSTOMER".equalsIgnoreCase(form.getRole())) {

            final Long staffId = savedUser.getUserId();

            StaffProfile staffProfile = staffProfileRepository.findById(staffId)
                    .orElseGet(() -> {
                        StaffProfile sp = new StaffProfile();
                        sp.setStaffId(staffId);     // StaffId = UserId (theo CSDL ver2)
                        return sp;
                    });

            staffProfile.setStaffType(form.getRole());
            staffProfile.setHireDate(form.getHireDate());
            staffProfile.setSpecialization(form.getSpecialization());
            staffProfile.setLicenseNumber(form.getLicenseNumber());

            staffProfileRepository.save(staffProfile);
        }

        redirectAttributes.addFlashAttribute(
                "message",
                isEdit ? "Đã cập nhật người dùng." : "Đã tạo người dùng mới."
        );

        return "redirect:/admin/users";
    }

    // ================== TOGGLE ACTIVE / LOCKED ==================
    @PostMapping("/users/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long userId,
                               RedirectAttributes redirectAttributes) {

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(user);

        String msg = user.isActive()
                ? "Đã mở khóa tài khoản."
                : "Đã khóa tài khoản.";
        redirectAttributes.addFlashAttribute("message", msg);

        return "redirect:/admin/users";
    }
}
