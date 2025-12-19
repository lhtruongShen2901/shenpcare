package g6shenpcare.service;

import g6shenpcare.entity.*;
import g6shenpcare.repository.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CatalogService {

    private final ServicesRepository servicesRepo;
    private final ServiceCategoryRepository categoryRepo;
    private final ServicePricingMatrixRepository pricingRepo;
    private final FileStorageRepository fileStorageRepo;
    private final ServiceComboItemsRepository comboItemsRepo;
    private final PetRepository petRepo;

    public CatalogService(ServicesRepository servicesRepo,
            ServiceCategoryRepository categoryRepo,
            ServicePricingMatrixRepository pricingRepo,
            FileStorageRepository fileStorageRepo,
            ServiceComboItemsRepository comboItemsRepo,
            PetRepository petRepo) {
        this.servicesRepo = servicesRepo;
        this.categoryRepo = categoryRepo;
        this.pricingRepo = pricingRepo;
        this.fileStorageRepo = fileStorageRepo;
        this.comboItemsRepo = comboItemsRepo;
        this.petRepo = petRepo;
    }

    // =========================================================
    // 1. QUẢN LÝ DỊCH VỤ (SERVICE CRUD)
    // =========================================================
    // Lấy danh sách chung (Cũ)
    public Page<Services> getServices(String type, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("sortOrder").ascending().and(Sort.by("serviceId").descending()));
        if ("ALL".equals(type)) {
            return servicesRepo.findAll(pageable);
        }
        return servicesRepo.findByServiceType(type, pageable);
    }

    // [MỚI] Lấy dịch vụ theo hệ thống (SPA hoặc CLINIC)
    public Page<Services> getServicesBySystem(String systemType, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("sortOrder").ascending().and(Sort.by("serviceId").descending()));
        return servicesRepo.findByCategoryType(systemType, pageable);
    }

    public Services getServiceById(Integer id) {
        return servicesRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Service not found"));
    }

    @Transactional
    public Services saveService(Services service, MultipartFile imageFile) throws IOException {
        // 1. Xử lý upload ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (service.getServiceId() != null) {
                Services old = servicesRepo.findById(service.getServiceId()).orElse(null);
                if (old != null && old.getImageFileId() != null) {
                    fileStorageRepo.deleteById(old.getImageFileId());
                }
            }
            // Lưu ảnh mới
            FileStorage file = new FileStorage();
            file.setFileName(imageFile.getOriginalFilename());
            file.setContentType(imageFile.getContentType());
            file.setFileSizeBytes(imageFile.getSize());
            file.setData(imageFile.getBytes());
            file.setUploadedAt(LocalDateTime.now());
            FileStorage savedFile = fileStorageRepo.save(file);
            service.setImageFileId(savedFile.getFileId());
        } else if (service.getServiceId() != null) {
            // Giữ lại ảnh cũ nếu không upload mới
            Services old = servicesRepo.findById(service.getServiceId()).orElse(null);
            if (old != null) {
                service.setImageFileId(old.getImageFileId());
                if (service.getVersion() == null) {
                    service.setVersion(old.getVersion());
                }
            }
        }

        // 2. Thiết lập giá trị mặc định
        if ("FIXED".equals(service.getPriceModel()) && service.getFixedPrice() == null) {
            service.setFixedPrice(BigDecimal.ZERO);
        }
        if (service.getVersion() == null) {
            service.setVersion(0);
        }

        // 3. Lưu xuống DB
        return servicesRepo.save(service);
    }

    @Transactional
    public void deleteService(Integer id) {
        servicesRepo.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Integer id) {
        Services s = getServiceById(id);
        s.setActive(!s.isActive());
        servicesRepo.save(s);
    }

    // =========================================================
    // 2. TÍNH NĂNG NÂNG CAO (DUPLICATE, COMBO)
    // =========================================================
    @Transactional
    public void duplicateService(Integer originalId) {
        Services original = getServiceById(originalId);
        Services copy = new Services();
        BeanUtils.copyProperties(original, copy, "serviceId", "version", "comboItems");

        copy.setName(original.getName() + " (Copy)");
        copy.setActive(false);
        copy.setShowOnWeb(false);

        Services savedCopy = servicesRepo.save(copy);

        // Copy bảng giá Matrix nếu có
        if ("MATRIX".equals(original.getPriceModel())) {
            List<ServicePricingMatrix> matrices = pricingRepo.findByServiceId(originalId);
            for (ServicePricingMatrix m : matrices) {
                ServicePricingMatrix mCopy = new ServicePricingMatrix();
                BeanUtils.copyProperties(m, mCopy, "pricingId", "serviceId", "createdAt");
                mCopy.setServiceId(savedCopy.getServiceId());
                mCopy.setCreatedAt(LocalDateTime.now());
                pricingRepo.save(mCopy);
            }
        }
    }

    @Transactional
    public void createCombo(Services combo, MultipartFile img, List<Integer> childIds) throws IOException {
        combo.setServiceType("COMBO");
        combo.setPriceModel("FIXED");

        // Lưu Combo để có ID trước
        Services savedCombo = saveService(combo, img);

        // Xóa các item cũ nếu đang update
        List<ServiceComboItems> existing = comboItemsRepo.findByComboServiceId(savedCombo.getServiceId());
        comboItemsRepo.deleteAll(existing);

        // Thêm các item mới
        if (childIds != null) {
            for (Integer childId : childIds) {
                ServiceComboItems item = new ServiceComboItems();
                item.setComboServiceId(savedCombo.getServiceId());
                item.setSingleServiceId(childId);
                comboItemsRepo.save(item);
            }
        }
    }

    // =========================================================
    // 3. LOGIC TÍNH GIÁ (HYBRID PRICING ENGINE)
    // =========================================================
    public BigDecimal calculateFinalPrice(Integer serviceId, Integer petId) {
        Services s = getServiceById(serviceId);
        Pets p = petRepo.findById(petId).orElseThrow(() -> new IllegalArgumentException("Pet not found"));

        // 1. Giá cố định
        if ("FIXED".equals(s.getPriceModel())) {
            return s.getFixedPrice();
        }

        // 2. Giá theo đơn vị (VD: 50k / KG)
        if ("PER_UNIT".equals(s.getPriceModel()) && "KG".equals(s.getPriceUnit())) {
            BigDecimal weight = BigDecimal.valueOf(p.getWeightKg() != null ? p.getWeightKg() : 0);
            return s.getFixedPrice().multiply(weight);
        }

        // 3. Giá ma trận (Matrix) - Chỉ cho Spa
        if ("MATRIX".equals(s.getPriceModel())) {
            String species = p.getSpecies();
            String coat = (p.getCoatLength() != null) ? p.getCoatLength() : "SHORT";
            Float weight = (p.getWeightKg() != null) ? p.getWeightKg() : 0f;

            // Tìm giá khớp nhất
            List<ServicePricingMatrix> matches = pricingRepo.findMatchingPrice(
                    serviceId, species, coat, weight, PageRequest.of(0, 1)
            );

            if (!matches.isEmpty()) {
                return matches.get(0).getPrice();
            }
        }

        return BigDecimal.ZERO; // Fallback
    }

    // =========================================================
    // 4. QUẢN LÝ DANH MỤC (CATEGORY)
    // =========================================================
    public List<ServiceCategory> getCategories(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return categoryRepo.searchCategories(keyword.trim());
        }
        return categoryRepo.findAll();
    }

    // [MỚI] Lấy danh mục theo loại (SPA hoặc CLINIC)
    public List<ServiceCategory> getCategoriesByType(String type) {
        return categoryRepo.findByCategoryTypeAndActiveTrue(type);
    }

    public List<ServiceCategory> getAllCategoriesBySystem(String type) {
        return categoryRepo.findByCategoryType(type);
    }

    public List<ServiceCategory> getAllCategories() {
        return categoryRepo.findAll();
    }

    public List<ServiceCategory> getActiveCategories() {
        return categoryRepo.findByActiveTrue();
    }

    @Transactional
    public void saveCategory(ServiceCategory category, MultipartFile imageFile) throws IOException {
        // Xử lý ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            if (category.getServiceCategoryId() != null) {
                ServiceCategory old = categoryRepo.findById(category.getServiceCategoryId()).orElse(null);
                if (old != null && old.getImageFileId() != null) {
                    fileStorageRepo.deleteById(old.getImageFileId());
                }
            }
            FileStorage file = new FileStorage();
            file.setFileName(imageFile.getOriginalFilename());
            file.setContentType(imageFile.getContentType());
            file.setFileSizeBytes(imageFile.getSize());
            file.setData(imageFile.getBytes());
            file.setUploadedAt(LocalDateTime.now());
            FileStorage savedFile = fileStorageRepo.save(file);

            category.setImageFileId(savedFile.getFileId());
        } else if (category.getServiceCategoryId() != null) {
            ServiceCategory old = categoryRepo.findById(category.getServiceCategoryId()).orElse(null);
            if (old != null) {
                category.setImageFileId(old.getImageFileId());
            }
        }

        if (category.getServiceCategoryId() == null) {
            category.setActive(true);
        }
        categoryRepo.save(category);
    }

    // Xử lý xóa danh mục nâng cao (3 options)
    @Transactional
    public void deleteCategoryWithAdvancedOption(Integer catId, String action, Integer targetCatId, List<Integer> idsToMove) {
        ServiceCategory cat = categoryRepo.findById(catId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));

        if ("JUST_DELETE".equals(action)) {
            cat.setActive(false);
        } else if ("DISABLE_SERVICES".equals(action)) {
            servicesRepo.disableServicesByCategory(catId);
            cat.setActive(false);
        } else if ("MOVE_SERVICES".equals(action)) {
            if (targetCatId != null) {
                servicesRepo.moveServicesToCategory(catId, targetCatId);
            }
            cat.setActive(false);
        } else if ("SELECTIVE_MOVE".equals(action)) {
            List<Services> children = servicesRepo.findByServiceCategoryIdAndActiveTrue(catId);
            for (Services s : children) {
                if (idsToMove != null && idsToMove.contains(s.getServiceId())) {
                    if (targetCatId != null) {
                        s.setServiceCategoryId(targetCatId);
                    }
                } else {
                    s.setActive(false);
                }
                servicesRepo.save(s);
            }
            cat.setActive(false);
        }
        categoryRepo.save(cat);
    }

    // Hỗ trợ View
    public List<Services> getServicesByCategory(Integer catId) {
        return servicesRepo.findByServiceCategoryIdAndActiveTrue(catId);
    }

    public Map<Integer, Long> getCategoryCounts() {
        List<Object[]> results = servicesRepo.countServicesByCategory();
        Map<Integer, Long> map = new HashMap<>();
        for (Object[] row : results) {
            Integer catId = (Integer) row[0];
            Long count = (Long) row[1];
            map.put(catId, count);
        }
        return map;
    }

    @Transactional
    public void activateCategory(Integer id) {
        ServiceCategory cat = categoryRepo.findById(id).orElseThrow();
        cat.setActive(true);
        categoryRepo.save(cat);
    }

    @Transactional
    public void hardDeleteCategory(Integer id) {
        if (servicesRepo.countByServiceCategoryId(id) > 0) {
            throw new IllegalStateException("Không thể xóa danh mục đang chứa dịch vụ.");
        }
        categoryRepo.deleteById(id);
    }

    // =========================================================
    // 5. MATRIX UTILS
    // =========================================================
    public List<ServicePricingMatrix> getMatrix(Integer serviceId) {
        return pricingRepo.findByServiceId(serviceId);
    }

    @Transactional
    public void saveMatrix(ServicePricingMatrix m) {
        if (m.getCreatedAt() == null) {
            m.setCreatedAt(LocalDateTime.now());
        }
        pricingRepo.save(m);
    }

    @Transactional
    public void deleteMatrix(Integer id) {
        pricingRepo.deleteById(id);
    }

}
