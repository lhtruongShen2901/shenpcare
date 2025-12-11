package g6shenpcare.service;

import g6shenpcare.dto.UserForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.StaffProfileRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserAccountRepository userAccountRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository,
                       StaffProfileRepository staffProfileRepository,
                       BookingRepository bookingRepository,
                       PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccount> searchStaff(String role, String status, String keyword) {
        return userAccountRepository.searchStaff(role, status, keyword);
    }

    public List<UserAccount> searchCustomers(String status, String keyword) {
        return userAccountRepository.searchCustomers(status, keyword);
    }

    // [FIX] Integer
    public UserAccount getUserById(Integer id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
    }

    @Transactional
    public void saveUser(UserForm form) {
        String email = (form.getEmail() != null) ? form.getEmail().trim() : "";
        String phone = (form.getPhone() != null) ? form.getPhone().trim() : "";
        String username = (form.getUsername() != null) ? form.getUsername().trim() : "";

        // form.getUserId() bây giờ là Integer
        if (form.getUserId() == null) {
            if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
                throw new IllegalArgumentException("Tên đăng nhập '" + username + "' đã tồn tại.");
            }
            if (!email.isEmpty() && userAccountRepository.existsByEmailIgnoreCase(email)) {
                throw new IllegalArgumentException("Email '" + email + "' đã được sử dụng.");
            }
            if (!phone.isEmpty() && userAccountRepository.existsByPhone(phone)) {
                throw new IllegalArgumentException("SĐT '" + phone + "' đã được sử dụng.");
            }
        } else {
            if (!email.isEmpty() && userAccountRepository.existsByEmailIgnoreCaseAndUserIdNot(email, form.getUserId())) {
                throw new IllegalArgumentException("Email trùng với tài khoản khác.");
            }
            if (!phone.isEmpty() && userAccountRepository.existsByPhoneAndUserIdNot(phone, form.getUserId())) {
                throw new IllegalArgumentException("SĐT trùng với tài khoản khác.");
            }
        }

        UserAccount user;
        if (form.getUserId() != null) {
            user = userAccountRepository.findById(form.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            user = new UserAccount();
            user.setCreatedAt(LocalDateTime.now());
            user.setUsername(username);
        }

        user.setFullName(form.getFullName());
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(form.getRole());
        user.setActive(!"LOCKED".equalsIgnoreCase(form.getStatus()));
        user.setUpdatedAt(LocalDateTime.now());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword().trim()));
        }

        UserAccount savedUser = userAccountRepository.save(user);

        if (form.getRole() != null && !"CUSTOMER".equalsIgnoreCase(form.getRole())) {
            Integer staffId = savedUser.getUserId(); 
            
            StaffProfile staffProfile = staffProfileRepository.findById(staffId)
                    .orElseGet(() -> {
                        StaffProfile sp = new StaffProfile();
                        sp.setStaffId(staffId);
                        String prefix = (form.getRole().length() >= 3) ? form.getRole().substring(0, 3).toUpperCase() : "STF";
                        sp.setStaffCode(prefix + "-" + System.currentTimeMillis());
                        return sp;
                    });

            if (staffProfile.getStaffCode() == null || staffProfile.getStaffCode().isEmpty()) {
                String prefix = (form.getRole().length() >= 3) ? form.getRole().substring(0, 3).toUpperCase() : "STF";
                staffProfile.setStaffCode(prefix + "-" + System.currentTimeMillis());
            }

            staffProfile.setStaffType(form.getRole());
            staffProfile.setHireDate(form.getHireDate());
            staffProfile.setSpecialization(form.getSpecialization());
            staffProfile.setLicenseNumber(form.getLicenseNumber());

            staffProfileRepository.save(staffProfile);
        }
    }

    // [FIX] Integer targets
    @Transactional
    public void deactivateStaffSafe(Integer targetStaffId, Integer replacementStaffId, String currentUsername) {
        UserAccount targetUser = userAccountRepository.findById(targetStaffId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (targetUser.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new IllegalStateException("Bạn không thể tự khóa tài khoản của chính mình!");
        }
        
        if (!targetUser.isActive()) {
            targetUser.setActive(true);
            targetUser.setUpdatedAt(LocalDateTime.now());
            userAccountRepository.save(targetUser);
            return;
        }

        if (!"CUSTOMER".equalsIgnoreCase(targetUser.getRole())) {
            long activeJobs = bookingRepository.countActiveBookingsByStaff(targetStaffId);

            if (activeJobs > 0) {
                if (replacementStaffId == null) {
                    throw new IllegalStateException("Nhân viên này còn " + activeJobs + " lịch hẹn chưa hoàn thành. Vui lòng chọn người bàn giao trước khi khóa!");
                }
                
                UserAccount replacementUser = getUserById(replacementStaffId);
                if (!replacementUser.isActive()) {
                     throw new IllegalArgumentException("Nhân viên thay thế đang bị khóa.");
                }
                if (!replacementUser.getRole().equals(targetUser.getRole())) {
                     throw new IllegalArgumentException("Người thay thế phải cùng chức vụ (" + targetUser.getRole() + ")");
                }

                bookingRepository.reassignBookings(targetStaffId, replacementStaffId);
            }
        }

        targetUser.setActive(false);
        targetUser.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(targetUser);
    }

    // [FIX] Integer userId - Đây là hàm sửa lỗi trong AdminCustomerController
    @Transactional
    public void toggleUserStatus(Integer userId, String currentUsername) {
        deactivateStaffSafe(userId, null, currentUsername);
    }

    // [FIX] Integer userId
    @Transactional
    public void changePassword(Integer userId, String newPassword) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(user);
    }

    // [FIX] Integer id
    @Transactional
    public void deleteUser(Integer id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại."));

        long bookingCount = bookingRepository.countActiveBookingsByStaff(id);
        if (bookingCount > 0) {
            throw new IllegalStateException("Nhân viên này đang có lịch hẹn. Vui lòng chuyển giao công việc và Khóa tài khoản thay vì Xóa.");
        }

        staffProfileRepository.deleteById(id);
        userAccountRepository.delete(user);
    }
}