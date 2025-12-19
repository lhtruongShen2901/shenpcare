package g6shenpcare.controller.admin;

import g6shenpcare.entity.ServiceCategory;
import g6shenpcare.entity.ServicePricingMatrix;
import g6shenpcare.entity.Services;
import g6shenpcare.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/admin/services")
public class AdminSpaController {

    private final CatalogService catalogService;

    public AdminSpaController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Spa");
        model.addAttribute("activeMenu", "spa-services");
    }

    // =========================================================
    // 1. QUẢN LÝ DỊCH VỤ (SPA & GROOMING ONLY)
    // =========================================================
    @GetMapping
    public String listSpaServices(Model model, Principal principal,
            @RequestParam(name = "page", defaultValue = "1") Integer page) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Dịch vụ Spa & Grooming");

        // [LOGIC MỚI] Chỉ lấy dịch vụ thuộc nhóm SPA
        Page<Services> pageContent = catalogService.getServicesBySystem("SPA", page, 10);

        model.addAttribute("services", pageContent);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageContent.getTotalPages());

        // View riêng cho Spa
        return "admin/spa/services-list";
    }

    // Trang tạo Dịch vụ Spa Mới
    @GetMapping("/new")
    public String newSpaService(Model model, Principal principal) {
        addCommonHeader(model, principal);

        Services s = new Services();
        s.setServiceType("SINGLE");
        s.setPriceModel("FIXED"); // Mặc định Fixed, có thể đổi sang Matrix
        s.setPriceUnit("LẦN");
        s.setFixedPrice(BigDecimal.ZERO);
        s.setActive(true);
        s.setShowOnWeb(true);
        s.setSortOrder(0);

        model.addAttribute("service", s);
        // [LOGIC MỚI] Chỉ load danh mục thuộc SPA
        model.addAttribute("categories", catalogService.getCategoriesByType("SPA"));
        model.addAttribute("isEdit", false);

        return "admin/spa/service-detail";
    }

    // Trang tạo Combo (Chức năng đặc thù của Spa)
    @GetMapping("/new-combo")
    public String createCombo(Model model, Principal principal) {
        addCommonHeader(model, principal);

        Services s = new Services();
        s.setServiceType("COMBO");
        s.setPriceModel("FIXED");
        s.setTargetSpecies("BOTH");
        s.setActive(true);
        s.setFixedPrice(BigDecimal.ZERO);

        model.addAttribute("service", s);
        // [LOGIC MỚI] Chỉ load danh mục SPA
        model.addAttribute("categories", catalogService.getCategoriesByType("SPA"));

        // Load danh sách dịch vụ con (Cũng nên lọc chỉ lấy SPA Single)
        // Tạm thời lấy hết Single, logic lọc kỹ hơn nằm ở Service
        model.addAttribute("singleServices", catalogService.getServices("SINGLE", 1, 100).getContent());

        return "admin/spa/combo-create";
    }

    // Xử lý lưu Combo
    @PostMapping("/save-combo")
    public String saveCombo(@ModelAttribute("service") Services combo,
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam(value = "childIds", required = false) List<Integer> childIds,
            RedirectAttributes ra) {
        try {
            catalogService.createCombo(combo, imageFile, childIds);
            ra.addFlashAttribute("message", "Đã tạo Combo Spa thành công.");
            return "redirect:/admin/services?tab=COMBO";
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/services/new-combo";
        }
    }

    // =========================================================
    // 2. CHỈNH SỬA & LƯU
    // =========================================================
    @GetMapping("/{id:\\d+}")
    public String editService(@PathVariable("id") Integer id, Model model, Principal principal) {
        addCommonHeader(model, principal);
        try {
            Services s = catalogService.getServiceById(id);

            // [AN TOÀN] Kiểm tra nếu đây là dịch vụ CLINIC thì đá sang Controller kia
            if (s.getServiceCategory() != null && "CLINIC".equals(s.getServiceCategory().getCategoryType())) {
                return "redirect:/admin/clinic/services/" + id;
            }

            model.addAttribute("service", s);
            model.addAttribute("categories", catalogService.getCategoriesByType("SPA"));
            model.addAttribute("isEdit", true);

            if ("COMBO".equals(s.getServiceType()) || s.isCombo()) {
                model.addAttribute("singleServices", catalogService.getServices("SINGLE", 1, 100).getContent());
                return "admin/spa/combo-create";
            }
            return "admin/spa/service-detail";
        } catch (Exception e) {
            return "redirect:/admin/services";
        }
    }

    @PostMapping("/save")
    public String saveService(@Valid @ModelAttribute("service") Services service,
            BindingResult bindingResult,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model, RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", catalogService.getCategoriesByType("SPA"));
            model.addAttribute("isEdit", service.getServiceId() != null);
            return "admin/spa/service-detail";
        }
        try {
            catalogService.saveService(service, imageFile);
            ra.addFlashAttribute("message", "Đã lưu dịch vụ Spa.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // =========================================================
    // 3. ACTIONS (DELETE, DUPLICATE, TOGGLE)
    // =========================================================
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes ra) {
        try {
            catalogService.deleteService(id);
            ra.addFlashAttribute("message", "Đã xóa dịch vụ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Không thể xóa (Dịch vụ đang có đơn hàng).");
        }
        return "redirect:/admin/services";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable("id") Integer id, RedirectAttributes ra) {
        catalogService.toggleStatus(id);
        ra.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        return "redirect:/admin/services";
    }

    @PostMapping("/duplicate")
    public String duplicate(@RequestParam("id") Integer id, RedirectAttributes ra) {
        catalogService.duplicateService(id);
        ra.addFlashAttribute("message", "Đã nhân bản dịch vụ.");
        return "redirect:/admin/services";
    }

    // =========================================================
    // 4. API CHO MATRIX PRICING (Đặc thù Spa)
    // =========================================================
    // API lấy list (giữ nguyên cho Ajax call nếu cần)
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<Services>> getServicesApi(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "type", defaultValue = "ALL") String type) {
        // Vẫn cho phép lấy ALL qua API nếu cần, hoặc có thể giới hạn lại SPA
        return ResponseEntity.ok(catalogService.getServices(type, page, size));
    }

    @GetMapping("/{id}/matrix")
    @ResponseBody
    public ResponseEntity<List<ServicePricingMatrix>> getMatrix(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(catalogService.getMatrix(id));
    }

    @PostMapping("/matrix/save")
    @ResponseBody
    public ResponseEntity<?> saveMatrix(@RequestBody ServicePricingMatrix matrix) {
        try {
            catalogService.saveMatrix(matrix);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/matrix/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMatrix(@PathVariable("id") Integer id) {
        catalogService.deleteMatrix(id);
        return ResponseEntity.ok().build();
    }

    // =========================================================
    // 5. QUẢN LÝ DANH MỤC (CHỈ SPA CATEGORIES)
    // =========================================================
    @GetMapping("/categories")
    public String categories(@RequestParam(name = "keyword", required = false) String keyword,
            Model model, Principal principal) {
        addCommonHeader(model, principal);

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm kiếm (Lưu ý: Bạn có thể cần update hàm search để chỉ tìm trong SPA nếu muốn kỹ hơn)
            model.addAttribute("categories", catalogService.getCategories(keyword));
        } else {
            // [SỬA LẠI ĐOẠN NÀY] 
            // Thay vì dùng getCategoriesByType (chỉ lấy Active), ta dùng hàm lấy tất cả
            model.addAttribute("categories", catalogService.getAllCategoriesBySystem("SPA"));
        }

        model.addAttribute("serviceCounts", catalogService.getCategoryCounts());
        model.addAttribute("keyword", keyword);
        model.addAttribute("pageTitle", "Danh mục Spa & Grooming");

        return "admin/spa/categories";
    }

    @GetMapping("/categories/new")
    public String newCategory(Model model, Principal principal) {
        addCommonHeader(model, principal);

        ServiceCategory cat = new ServiceCategory();
        cat.setActive(true);
        cat.setCategoryType("SPA"); // [QUAN TRỌNG] Mặc định là SPA

        model.addAttribute("category", cat);
        model.addAttribute("pageTitle", "Thêm Danh mục Spa Mới");
        model.addAttribute("isEdit", false);

        return "admin/spa/category-detail";
    }

    @GetMapping("/categories/{id}")
    public String editCategory(@PathVariable("id") Integer id, Model model, Principal principal) {
        addCommonHeader(model, principal);

        ServiceCategory cat = catalogService.getAllCategories().stream()
                .filter(c -> c.getServiceCategoryId().equals(id))
                .findFirst()
                .orElse(new ServiceCategory());

        // Validate chéo
        if ("CLINIC".equals(cat.getCategoryType())) {
            return "redirect:/admin/clinic/specialties";
        }

        model.addAttribute("category", cat);
        model.addAttribute("pageTitle", "Chỉnh sửa Danh mục");
        model.addAttribute("isEdit", true);

        return "admin/spa/category-detail";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute ServiceCategory cat,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes ra) {
        try {
            // Force Type luôn là SPA để tránh nhầm lẫn
            if (cat.getCategoryType() == null || cat.getCategoryType().isEmpty()) {
                cat.setCategoryType("SPA");
            }

            catalogService.saveCategory(cat, imageFile);
            ra.addFlashAttribute("message", "Đã lưu danh mục thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/services/categories/new";
        }
        return "redirect:/admin/services/categories";
    }

    @PostMapping("/categories/delete")
    public String deleteCategoryProcess(
            @RequestParam("id") Integer catId,
            @RequestParam("action") String action,
            @RequestParam(value = "targetId", required = false) Integer targetCatId,
            @RequestParam(value = "selectedIds", required = false) List<Integer> selectedIds,
            RedirectAttributes ra) {

        try {
            if (("MOVE_SERVICES".equals(action) || "SELECTIVE_MOVE".equals(action)) && (targetCatId == null || targetCatId.equals(catId))) {
                ra.addFlashAttribute("error", "Vui lòng chọn danh mục đích hợp lệ.");
                return "redirect:/admin/services/categories";
            }
            catalogService.deleteCategoryWithAdvancedOption(catId, action, targetCatId, selectedIds);
            ra.addFlashAttribute("message", "Đã xử lý danh mục thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/services/categories";
    }

    @PostMapping("/categories/{id}/activate")
    public String activateCategory(@PathVariable("id") Integer id, RedirectAttributes ra) {
        catalogService.activateCategory(id);
        ra.addFlashAttribute("message", "Đã kích hoạt lại danh mục.");
        return "redirect:/admin/services/categories";
    }

    @PostMapping("/categories/delete-hard")
    public String deleteCategoryPermanently(@RequestParam("id") Integer id, RedirectAttributes ra) {
        try {
            catalogService.hardDeleteCategory(id);
            ra.addFlashAttribute("message", "Đã xóa vĩnh viễn danh mục.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/services/categories";
    }

    // [BỔ SUNG] API lấy danh sách dịch vụ theo danh mục (Dùng cho Modal xem nhanh & Move)
    @GetMapping("/api/services-by-category")
    @ResponseBody
    public ResponseEntity<List<Services>> getServicesByCat(@RequestParam("id") Integer catId) {
        return ResponseEntity.ok(catalogService.getServicesByCategory(catId));
    }
}
