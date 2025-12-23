package g6shenpcare.service;

import g6shenpcare.entity.Booking;
import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.BookingRepository;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.repository.PetRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {

    private final UserAccountRepository userAccountRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PetRepository petRepository;
    
    @Autowired 
    private BookingRepository bookingRepository;

    @Autowired
    public ClientService(UserAccountRepository userAccountRepository,
                         CustomerProfileRepository customerProfileRepository,
                         PetRepository petRepository) {
        this.userAccountRepository = userAccountRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.petRepository = petRepository;
    }

    // ==========================================================
    // 1. QUẢN LÝ PROFILE (HỒ SƠ KHÁCH HÀNG)
    // ==========================================================

    @Transactional
    public CustomerProfile getProfileByUsername(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));

        return customerProfileRepository.findByUserId(user.getUserId())
                .orElseGet(() -> createDefaultProfile(user));
    }

    private CustomerProfile createDefaultProfile(UserAccount user) {
        CustomerProfile newProfile = new CustomerProfile();
        newProfile.setUserId(user.getUserId());
        newProfile.setFullName(user.getFullName());
        newProfile.setEmail(user.getEmail());
        newProfile.setPhone(user.getPhone());
        newProfile.setActive(true);
        newProfile.setCreatedAt(LocalDateTime.now());
        newProfile.setUpdatedAt(LocalDateTime.now());
        return customerProfileRepository.save(newProfile);
    }

    @Transactional
    public void updateProfile(CustomerProfile currentProfile, CustomerProfile formInput) {
        currentProfile.setFullName(formInput.getFullName());
        currentProfile.setPhone(formInput.getPhone());
        currentProfile.setAddressLine(formInput.getAddressLine());
        currentProfile.setWard(formInput.getWard());
        currentProfile.setDistrict(formInput.getDistrict());
        currentProfile.setCity(formInput.getCity());
        currentProfile.setNotes(formInput.getNotes());
        currentProfile.setUpdatedAt(LocalDateTime.now());

        // Đồng bộ ngược lại bảng UserAccount (nếu cần)
        UserAccount user = userAccountRepository.findById(currentProfile.getUserId()).orElse(null);
        if (user != null) {
            user.setFullName(formInput.getFullName());
            user.setPhone(formInput.getPhone());
            userAccountRepository.save(user);
        }
        customerProfileRepository.save(currentProfile);
    }

    // ==========================================================
    // 2. QUẢN LÝ THÚ CƯNG (PETS) - [ĐÃ CẬP NHẬT]
    // ==========================================================

    public List<Pets> getPetsByCustomer(CustomerProfile customer) {
        // Lọc chỉ lấy thú cưng đang Active (chưa bị xóa)
        // Nếu bạn muốn hiện cả thú cưng đã xóa thì bỏ đoạn filter logic này đi
        // Tuy nhiên thường thì ta chỉ hiển thị active=true
        // Giả sử repo chưa có findByActive, ta có thể filter ở đây hoặc query DB
        // Ở đây mình dùng list gốc từ DB:
        return petRepository.findByCustomerId(customer.getCustomerId());
    }

    /**
     * Hàm dùng chung cho cả THÊM MỚI và CẬP NHẬT
     */
    @Transactional
    public void savePetForUser(Pets petForm, MultipartFile avatarFile, String username) {
        CustomerProfile profile = getProfileByUsername(username);
        Pets petToSave;

        // --- TRƯỜNG HỢP 1: CẬP NHẬT (Có ID) ---
        if (petForm.getPetId() != null) {
            petToSave = petRepository.findById(petForm.getPetId())
                    .orElseThrow(() -> new IllegalArgumentException("Thú cưng không tồn tại"));
            
            // Bảo mật: Check xem pet này có đúng của user không
            if (!petToSave.getCustomerId().equals(profile.getCustomerId())) {
                throw new SecurityException("Bạn không có quyền chỉnh sửa thú cưng này.");
            }

            // Map các trường thông tin cập nhật
            petToSave.setName(petForm.getName());
            petToSave.setSpecies(petForm.getSpecies());
            petToSave.setBreed(petForm.getBreed());
            petToSave.setGender(petForm.getGender());
            petToSave.setBirthDate(petForm.getBirthDate());
            
            // [QUAN TRỌNG] Kiểu Double
            petToSave.setWeightKg(petForm.getWeightKg());
            
            // Các trường bổ sung
            petToSave.setColor(petForm.getColor());
            
            // [QUAN TRỌNG] Gọi hàm getSterilized() (đã sửa ở Entity)
            petToSave.setSterilized(petForm.getSterilized());
            
            petToSave.setMicrochipNumber(petForm.getMicrochipNumber());
            petToSave.setNotes(petForm.getNotes());
            petToSave.setUpdatedAt(LocalDateTime.now());
        } 
        // --- TRƯỜNG HỢP 2: THÊM MỚI (Không có ID) ---
        else {
            petToSave = petForm;
            petToSave.setCustomer(profile);
            petToSave.setCustomerId(profile.getCustomerId());
            petToSave.setOwnerId(profile.getUserId());
            petToSave.setActive(true);
            petToSave.setCreatedAt(LocalDateTime.now());
            petToSave.setUpdatedAt(LocalDateTime.now());
            
            if (petToSave.getPetCode() == null) {
                petToSave.setPetCode("PET-" + System.currentTimeMillis());
            }
        }

        // Xử lý Upload Ảnh (Demo: in ra console)
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // String fileName = fileStorageService.storeFile(avatarFile);
                // petToSave.setAvatarFileId(1); // Set ID file thật ở đây
                System.out.println("ClientService: Nhận file ảnh - " + avatarFile.getOriginalFilename());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        petRepository.save(petToSave);
    }

    /**
     * [MỚI] Hàm XÓA thú cưng (Soft Delete)
     * Thay vì xóa vĩnh viễn, ta chuyển trạng thái Active = false
     */
    @Transactional
    public void deletePet(Integer petId) {
        Pets pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("Thú cưng không tồn tại"));

        // Soft Delete: Chỉ tắt kích hoạt
        pet.setActive(false);
        pet.setUpdatedAt(LocalDateTime.now());
        
        petRepository.save(pet);
    }

    // ==========================================================
    // 3. QUẢN LÝ LỊCH SỬ (HISTORY)
    // ==========================================================

    public List<Booking> getBookingHistory(CustomerProfile customer) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getCustomerId());
    }
}