package g6shenpcare.service;

import g6shenpcare.dto.UserForm;
import g6shenpcare.entity.StaffProfile;
import g6shenpcare.entity.UserAccount;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userAccountRepository,
            StaffProfileRepository staffProfileRepository,
            PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccount> searchStaff(String role, String status, String keyword) {
        return userAccountRepository.searchStaff(role, status, keyword);
    }

    // Tìm Customer
    public List<UserAccount> searchCustomers(String status, String keyword) {
        return userAccountRepository.searchCustomers(status, keyword);
    }

    @Transactional
    public void saveUser(UserForm form) {
        UserAccount user;
        boolean isEdit = (form.getUserId() != null);

        if (isEdit) {
            user = userAccountRepository.findById(form.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
        } else {
            user = new UserAccount();
            user.setCreatedAt(LocalDateTime.now());
        }

        // 1. Map dữ liệu UserAccount
        user.setUsername(form.getUsername());
        user.setFullName(form.getFullName());
        user.setEmail(form.getEmail());
        user.setPhone(form.getPhone());
        user.setRole(form.getRole());
        user.setActive(!"LOCKED".equalsIgnoreCase(form.getStatus()));
        user.setUpdatedAt(LocalDateTime.now());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(form.getPassword().trim()));
        }

        UserAccount savedUser = userAccountRepository.save(user);

        // 2. Map dữ liệu StaffProfile (Nếu không phải Customer)
        if (form.getRole() != null && !"CUSTOMER".equalsIgnoreCase(form.getRole())) {
            Long staffId = savedUser.getUserId();

            StaffProfile staffProfile = staffProfileRepository.findById(staffId)
                    .orElseGet(() -> {
                        StaffProfile sp = new StaffProfile();
                        sp.setStaffId(staffId);

                        // Logic sinh mã nhân viên: 3 ký tự đầu Role + Timestamp
                        // VD: DOC-170123456789
                        String prefix = (form.getRole().length() >= 3)
                                ? form.getRole().substring(0, 3).toUpperCase()
                                : "STF";
                        sp.setStaffCode(prefix + "-" + System.currentTimeMillis());

                        return sp;
                    });

            // Đảm bảo StaffCode luôn có giá trị (phòng trường hợp data cũ bị null)
            if (staffProfile.getStaffCode() == null || staffProfile.getStaffCode().isEmpty()) {
                String prefix = (form.getRole().length() >= 3)
                        ? form.getRole().substring(0, 3).toUpperCase()
                        : "STF";
                staffProfile.setStaffCode(prefix + "-" + System.currentTimeMillis());
            }

            staffProfile.setStaffType(form.getRole());
            staffProfile.setHireDate(form.getHireDate());
            staffProfile.setSpecialization(form.getSpecialization());
            staffProfile.setLicenseNumber(form.getLicenseNumber());

            staffProfileRepository.save(staffProfile);
        }
    }

    public void toggleUserStatus(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(!user.isActive());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountRepository.save(user);
    }

    public UserAccount getUserById(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
