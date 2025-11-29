package g6shenpcare.service;

import g6shenpcare.entity.ServiceCategory;
import g6shenpcare.entity.Services;
import g6shenpcare.repository.ServiceCategoryRepository;
import g6shenpcare.repository.ServicesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClinicService {

    private final ServiceCategoryRepository categoryRepo;
    private final ServicesRepository servicesRepo;

    public ClinicService(ServiceCategoryRepository categoryRepo, ServicesRepository servicesRepo) {
        this.categoryRepo = categoryRepo;
        this.servicesRepo = servicesRepo;
    }

    // ================== SERVICE MANAGEMENT (QUẢN LÝ DỊCH VỤ) ==================

    public List<Services> getAllServices() {
        return servicesRepo.findAll();
    }

    public Services getServiceById(Integer id) {
        return servicesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found: " + id));
    }

    @Transactional
    public void saveService(Services service) {
        servicesRepo.save(service);
    }

    @Transactional
    public void toggleServiceStatus(Integer id) {
        Services s = getServiceById(id);
        s.setActive(!s.isActive());
        servicesRepo.save(s);
    }

    // ================== CATEGORY MANAGEMENT (QUẢN LÝ DANH MỤC) ==================

    public List<ServiceCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public List<ServiceCategory> getActiveCategories() {
        return categoryRepo.findByActiveTrue();
    }

    public ServiceCategory getCategoryById(Integer id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    @Transactional
    public void saveCategory(ServiceCategory category) {
        // Logic: Nếu tạo mới thì mặc định set Active = true
        if (category.getServiceCategoryId() == null) {
            category.setActive(true);
        }
        categoryRepo.save(category);
    }

    // ================== LOGIC XỬ LÝ RÀNG BUỘC KHI XÓA DANH MỤC ==================

    /**
     * 1. Đếm số dịch vụ con trong danh mục
     * Dùng để hiển thị cảnh báo hoặc kiểm tra trước khi xóa.
     */
    public long countServicesIn(Integer catId) {
        return servicesRepo.countByServiceCategoryId(catId);
    }

    /**
     * 2. Xử lý xóa/khóa danh mục với tùy chọn xử lý dịch vụ con (Constraint Handling)
     * @param catId ID danh mục cần xóa
     * @param actionType "DISABLE_SERVICES" (Khóa con) hoặc "MOVE_SERVICES" (Chuyển con)
     * @param targetCatId ID danh mục đích (chỉ dùng khi actionType là MOVE_SERVICES)
     */
    @Transactional
    public void deleteCategoryWithOption(Integer catId, String actionType, Integer targetCatId) {
        ServiceCategory cat = getCategoryById(catId);

        if ("MOVE_SERVICES".equals(actionType) && targetCatId != null) {
            // Chuyển toàn bộ dịch vụ con sang danh mục mới
            servicesRepo.moveServicesToCategory(catId, targetCatId);
        } else if ("DISABLE_SERVICES".equals(actionType)) {
            // Khóa toàn bộ dịch vụ con
            servicesRepo.disableServicesByCategory(catId);
        }
        
        // Sau khi xử lý con xong -> Khóa danh mục cha (Soft Delete)
        cat.setActive(false);
        categoryRepo.save(cat);
    }

    /**
     * 3. Mở khóa lại danh mục (Active = true)
     */
    @Transactional
    public void activateCategory(Integer id) {
        ServiceCategory cat = getCategoryById(id);
        cat.setActive(true);
        categoryRepo.save(cat);
    }
}