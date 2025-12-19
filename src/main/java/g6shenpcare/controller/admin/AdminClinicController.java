package g6shenpcare.controller.admin;

import g6shenpcare.entity.ClinicalTemplate;
import g6shenpcare.entity.Product;
import g6shenpcare.entity.ServiceCategory;
import g6shenpcare.entity.Services;
import g6shenpcare.repository.ClinicalTemplateRepository;
import g6shenpcare.repository.ProductRepository;
import g6shenpcare.repository.ServiceCategoryRepository;
import g6shenpcare.repository.ServicesRepository;
import g6shenpcare.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/clinic")
public class AdminClinicController {

    private final ServiceCategoryRepository categoryRepo;
    private final ServicesRepository servicesRepo;
    private final ProductRepository productRepo;
    private final ClinicalTemplateRepository templateRepo;
    private final CatalogService catalogService;

    public AdminClinicController(ServiceCategoryRepository categoryRepo,
            ServicesRepository servicesRepo,
            ProductRepository productRepo,
            ClinicalTemplateRepository templateRepo,
            CatalogService catalogService) {
        this.categoryRepo = categoryRepo;
        this.servicesRepo = servicesRepo;
        this.productRepo = productRepo;
        this.templateRepo = templateRepo;
        this.catalogService = catalogService;
    }

    private void addCommonHeader(Model model, Principal principal, String activeMenu) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", activeMenu);
    }

    // =========================================================
    // 1. QUẢN LÝ DỊCH VỤ KHÁM CHỮA BỆNH
    // =========================================================
    @GetMapping("/services")
    public String listMedicalServices(Model model, Principal principal,
            @RequestParam(name = "page", defaultValue = "1") Integer page) {
        addCommonHeader(model, principal, "clinic-services");
        model.addAttribute("pageTitle", "Danh mục Khám & Điều Trị");

        // Chỉ lấy dịch vụ thuộc nhóm CLINIC
        Page<Services> pageContent = catalogService.getServicesBySystem("CLINIC", page, 10);

        model.addAttribute("services", pageContent);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageContent.getTotalPages());

        return "admin/clinic/service-list";
    }

    @GetMapping("/services/new")
    public String newMedicalService(Model model, Principal principal) {
        addCommonHeader(model, principal, "clinic-services");

        Services s = new Services();
        s.setServiceType("SINGLE");
        s.setPriceModel("FIXED");
        s.setPriceUnit("LẦN");
        s.setFixedPrice(BigDecimal.ZERO);
        s.setActive(true);
        s.setShowOnWeb(true);
        s.setSortOrder(0);

        model.addAttribute("service", s);
        model.addAttribute("categories", catalogService.getCategoriesByType("CLINIC"));
        model.addAttribute("isEdit", false);

        return "admin/clinic/service-detail";
    }

    @GetMapping("/services/{id}")
    public String editMedicalService(@PathVariable("id") Integer id, Model model, Principal principal) {
        addCommonHeader(model, principal, "clinic-services");
        try {
            Services s = catalogService.getServiceById(id);

            // Validate chéo: Nếu là Spa thì đá về Controller Spa
            if (s.getServiceCategory() != null && "SPA".equals(s.getServiceCategory().getCategoryType())) {
                return "redirect:/admin/services/" + id;
            }

            model.addAttribute("service", s);
            model.addAttribute("categories", catalogService.getCategoriesByType("CLINIC"));
            model.addAttribute("isEdit", true);

            return "admin/clinic/service-detail";
        } catch (Exception e) {
            return "redirect:/admin/clinic/services";
        }
    }

    @PostMapping("/services/save")
    public String saveMedicalService(@Valid @ModelAttribute("service") Services service,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model, RedirectAttributes ra) {
        // Force chuẩn y tế
        service.setServiceType("SINGLE");
        service.setCombo(false);

        if (bindingResult.hasErrors()) {
            addCommonHeader(model, null, "clinic-services");
            model.addAttribute("categories", catalogService.getCategoriesByType("CLINIC"));
            model.addAttribute("isEdit", service.getServiceId() != null);
            return "admin/clinic/service-detail";
        }
        try {
            catalogService.saveService(service, imageFile);
            ra.addFlashAttribute("message", "Đã lưu dịch vụ y tế.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/clinic/services";
    }

    // =========================================================
    // 2. CÁC HÀNH ĐỘNG (XÓA, ẨN/HIỆN, NHÂN BẢN)
    // =========================================================
    @PostMapping("/services/delete")
    public String deleteMedicalService(@RequestParam("id") Integer id, RedirectAttributes ra) {
        try {
            catalogService.deleteService(id);
            ra.addFlashAttribute("message", "Đã xóa dịch vụ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa (Dịch vụ đã phát sinh hồ sơ khám).");
        }
        return "redirect:/admin/clinic/services";
    }

    // [BỔ SUNG] Hàm xử lý nút Toggle trên danh sách
    @PostMapping("/services/{id}/toggle")
    public String toggleService(@PathVariable("id") Integer id, RedirectAttributes ra) {
        catalogService.toggleStatus(id);
        // Không cần thông báo để thao tác nhanh hơn
        return "redirect:/admin/clinic/services";
    }

    // [BỔ SUNG] Hàm xử lý nút Nhân bản
    @PostMapping("/services/duplicate")
    public String duplicateService(@RequestParam("id") Integer id, RedirectAttributes ra) {
        catalogService.duplicateService(id);
        ra.addFlashAttribute("message", "Đã nhân bản dịch vụ thành công.");
        return "redirect:/admin/clinic/services";
    }

    // =========================================================
    // 3. QUẢN LÝ CHUYÊN KHOA (SPECIALTIES)
    // =========================================================
    @GetMapping("/specialties")
    public String manageSpecialties(Model model, Principal principal,
            @RequestParam(name = "keyword", required = false) String keyword) {
        addCommonHeader(model, principal, "specialties");
        model.addAttribute("pageTitle", "Quản lý Chuyên Khoa");

        List<ServiceCategory> clinics;
        if (keyword != null && !keyword.trim().isEmpty()) {
            clinics = categoryRepo.searchClinic("CLINIC", keyword.trim());
        } else {
            clinics = categoryRepo.findByCategoryType("CLINIC");
        }

        model.addAttribute("specialties", clinics);
        model.addAttribute("keyword", keyword);

        // [BỔ SUNG QUAN TRỌNG] Thêm dòng này để lấy số lượng dịch vụ
        model.addAttribute("serviceCounts", catalogService.getCategoryCounts());

        return "admin/clinic/specialties";
    }

    @PostMapping("/specialties/save")
    public String saveSpecialty(@ModelAttribute ServiceCategory specialty,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile, // [1] Thêm tham số này
            RedirectAttributes ra) {
        try {
            specialty.setCategoryType("CLINIC"); // Luôn gán là CLINIC

            // [2] Xử lý các giá trị mặc định cho trường hợp Tạo Mới
            if (specialty.getServiceCategoryId() == null) {
                // Nếu không nhập link icon thì lấy mặc định
                if (specialty.getIconUrl() == null || specialty.getIconUrl().isEmpty()) {
                    specialty.setIconUrl("/img/default-specialty.png");
                }
                specialty.setIsActive(true);
            }

            // [3] Gọi CatalogService để vừa lưu thông tin, vừa xử lý file ảnh
            // (Không dùng categoryRepo.save trực tiếp ở đây nữa)
            catalogService.saveCategory(specialty, imageFile);

            ra.addFlashAttribute("message", "Lưu chuyên khoa thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/clinic/specialties";
    }

    @GetMapping("/specialties/delete/{id}")
    public String deleteSpecialty(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            long count = servicesRepo.countServicesInClinic(id);
            if (count > 0) {
                ra.addFlashAttribute("error", "Không thể xóa! Khoa này đang chứa " + count + " dịch vụ.");
            } else {
                categoryRepo.deleteById(id);
                ra.addFlashAttribute("message", "Đã xóa chuyên khoa vĩnh viễn.");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }
        return "redirect:/admin/clinic/specialties";
    }

    @GetMapping("/api/specialty/{id}")
    @ResponseBody
    public ServiceCategory getSpecialtyApi(@PathVariable("id") Integer id) {
        return categoryRepo.findById(id).orElse(new ServiceCategory());
    }

// =========================================================
    // 4. KHO DƯỢC & VẬT TƯ (PHARMACY) - ĐÃ CẬP NHẬT
    // =========================================================
    @GetMapping("/pharmacy")
    public String managePharmacy(Model model, Principal principal,
            @RequestParam(name = "keyword", required = false) String keyword) {
        addCommonHeader(model, principal, "inventory");
        model.addAttribute("pageTitle", "Kho Dược & Vật Tư Y Tế");

        List<Product> medicines;
        if (keyword != null && !keyword.trim().isEmpty()) {
            medicines = productRepo.searchMedicines(keyword.trim());
        } else {
            medicines = productRepo.findByIsMedicineTrueAndIsActiveTrue();
        }

        model.addAttribute("products", medicines);
        model.addAttribute("keyword", keyword);

        // Thêm object rỗng để form modal binding
        model.addAttribute("newProduct", new Product());

        return "admin/clinic/pharmacy";
    }

    // API lấy chi tiết thuốc để hiển thị lên Modal Sửa (Dùng JS fetch)
    @GetMapping("/api/product/{id}")
    @ResponseBody
    public Product getProductApi(@PathVariable("id") Integer id) {
        return productRepo.findById(id).orElse(new Product());
    }

    @PostMapping("/pharmacy/save")
    public String saveProduct(@ModelAttribute Product product,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes ra) {
        try {
            product.setIsMedicine(true); // Luôn đánh dấu là hàng Y tế

            // Logic xử lý ảnh (Tương tự Specialty)
            if (product.getProductId() != null) {
                Product old = productRepo.findById(product.getProductId()).orElse(null);
                if (old != null) {
                    if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                        product.setImageUrl(old.getImageUrl());
                    }
                    if (imageFile == null || imageFile.isEmpty()) {
                        product.setImageFileId(old.getImageFileId());
                    }
                    product.setCreatedAt(old.getCreatedAt());
                }
            } else {
                product.setCreatedAt(java.time.LocalDateTime.now());
            }
            product.setUpdatedAt(java.time.LocalDateTime.now());

            // Gọi Service lưu file (Tái sử dụng CatalogService hoặc viết logic save trực tiếp nếu cần nhanh)
            // Ở đây mình gọi qua catalogService nếu bạn đã update hàm upload ở bước trước, 
            // nếu chưa thì dùng tạm productRepo.save(product) và bỏ qua file ảnh.
            // Giả sử dùng tạm Repo (chưa upload ảnh file):
            productRepo.save(product);

            ra.addFlashAttribute("message", "Đã lưu thông tin thuốc/vật tư.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/clinic/pharmacy";
    }

    @GetMapping("/pharmacy/delete/{id}")
    public String deleteProduct(@PathVariable("id") Integer id, RedirectAttributes ra) {
        try {
            // Soft delete (Chỉ ẩn đi chứ không xóa thật để giữ lịch sử đơn thuốc)
            Product p = productRepo.findById(id).orElseThrow();
            p.setIsActive(false);
            productRepo.save(p);
            ra.addFlashAttribute("message", "Đã xóa thuốc khỏi danh sách hiển thị.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/clinic/pharmacy";
    }

    // =========================================================
    // 5. MẪU BỆNH ÁN (TEMPLATES)
    // =========================================================
    @GetMapping("/templates")
    public String manageTemplates(Model model, Principal principal) {
        addCommonHeader(model, principal, "templates");
        model.addAttribute("pageTitle", "Cấu hình Mẫu Khám");

        List<ClinicalTemplate> templates = templateRepo.findAll();
        model.addAttribute("templates", templates);
        return "admin/clinic/templates-config";
    }
}
