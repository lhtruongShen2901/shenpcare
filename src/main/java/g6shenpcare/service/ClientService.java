package g6shenpcare.service;

import g6shenpcare.entity.CustomerProfile;
import g6shenpcare.entity.Pets;
import g6shenpcare.entity.UserAccount;
import g6shenpcare.repository.CustomerProfileRepository;
import g6shenpcare.repository.PetRepository;
import g6shenpcare.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import g6shenpcare.entity.Booking;
import g6shenpcare.repository.BookingRepository;

import java.io.IOException; // Nếu sau này bạn xử lý file thật
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClientService {

    private final UserAccountRepository userAccountRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PetRepository petRepository;
    @Autowired private BookingRepository bookingRepository;

    @Autowired
    public ClientService(UserAccountRepository userAccountRepository,
                         CustomerProfileRepository customerProfileRepository,
                         PetRepository petRepository) {
        this.userAccountRepository = userAccountRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.petRepository = petRepository;
    }

    /**
     * 1. Lấy (hoặc tạo mới) hồ sơ khách hàng dựa trên Username đăng nhập
     * Giúp tránh lỗi NullPointerException khi User mới đăng ký chưa có Profile
     */
    @Transactional
    public CustomerProfile getProfileByUsername(String username) {
        // B1: Tìm UserAccount gốc
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản: " + username));

        // B2: Tìm CustomerProfile dựa trên userId
        return customerProfileRepository.findByUserId(user.getUserId())
                .orElseGet(() -> createDefaultProfile(user));
    }

    /**
     * Hàm phụ: Tạo profile mặc định nếu chưa có
     */
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

    /**
     * 2. Lấy danh sách thú cưng của khách hàng
     */
    public List<Pets> getPetsByCustomer(CustomerProfile customer) {
        return petRepository.findByCustomerId(customer.getCustomerId());
    }

    /**
     * 3. THÊM MỚI THÚ CƯNG (FIX LỖI LƯU ẢO)
     * Logic: Gán chặt chẽ Customer và OwnerId vào Pet trước khi Save
     */
    @Transactional
    public void addNewPet(CustomerProfile customer, Pets pet, MultipartFile avatarFile) {
        
        // --- LIÊN KẾT DỮ LIỆU (QUAN TRỌNG) ---
        pet.setCustomer(customer);                // Gán Object (để JPA hiểu quan hệ)
        pet.setCustomerId(customer.getCustomerId()); // Gán ID (để chắc chắn lưu vào cột CustomerId)
        pet.setOwnerId(customer.getUserId());     // Gán User ID gốc (để backup tra cứu)

        // Các thông tin mặc định
        if (pet.getPetCode() == null || pet.getPetCode().isEmpty()) {
            pet.setPetCode("PET-" + System.currentTimeMillis());
        }
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        pet.setActive(true);

        // --- XỬ LÝ ẢNH (GIẢ LẬP HOẶC THỰC TẾ) ---
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // TODO: Tại đây bạn có thể gọi service lưu file thật (ví dụ: FileStorageService)
                // String fileName = fileStorageService.storeFile(avatarFile);
                
                // Tạm thời mình lấy tên file gốc để demo logic
                String fileName = StringUtils.cleanPath(avatarFile.getOriginalFilename());
                
                // Giả sử ta lưu file ID hoặc đường dẫn vào trường avatarFileId hoặc url
                // Ở đây entity Pets của bạn dùng avatarFileId (Integer).
                // Nếu chưa có bảng Files, ta có thể tạm set cứng hoặc bỏ qua.
                // pet.setAvatarFileId(123); 
                
                System.out.println("ClientService: Đã nhận file ảnh: " + fileName);
            } catch (Exception e) {
                e.printStackTrace(); // Log lỗi nhưng không chặn việc lưu Pet
            }
        }

        // --- LƯU XUỐNG DB ---
        petRepository.save(pet);
    }
    
    /**
     * 4. Cập nhật thông tin hồ sơ khách hàng
     */
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

        // Đồng bộ ngược lại bảng UserAccount (nếu cần thiết tên, sđt)
        UserAccount user = userAccountRepository.findById(currentProfile.getUserId()).orElse(null);
        if (user != null) {
            user.setFullName(formInput.getFullName());
            user.setPhone(formInput.getPhone());
            userAccountRepository.save(user);
        }

        customerProfileRepository.save(currentProfile);
    }
    public List<Booking> getBookingHistory(CustomerProfile customer) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getCustomerId());
    }
}