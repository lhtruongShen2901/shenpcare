package g6shenpcare.controller.admin;

import g6shenpcare.entity.MasterWeightRange;
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

import java.security.Principal;
import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    private final CatalogService catalogService;

    public AdminServiceController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Spa");
        model.addAttribute("activeMenu", "services");
    }

    // =========================================================
    // 1. DASHBOARD & LIST
    // =========================================================
    @GetMapping
    public String listServices(Model model, Principal principal,
            @RequestParam(name = "page", defaultValue = "1") Integer page) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Dịch vụ");
        
        Page<Services> pageContent = catalogService.getServices("ALL", page, 10);
        model.addAttribute("services", pageContent);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageContent.getTotalPages());
        model.addAttribute("categories", catalogService.getAllCategories()); // Cho dropdown filter
        return "admin/services";
    }

    // =========================================================
    // 2. CÁC TRANG TẠO MỚI (URL TĨNH - QUAN TRỌNG ĐẶT TRƯỚC ID)
    // =========================================================

    // Trang tạo Dịch vụ Lẻ / Addon
    @GetMapping("/new")
    public String newService(@RequestParam(name = "type", defaultValue = "SINGLE") String type, 
                             Model model, Principal principal) {
        addCommonHeader(model, principal);
        Services s = new Services();
        
        // Khởi tạo giá trị mặc định tránh lỗi View
        s.setServiceType(type);
        s.setPriceModel("FIXED"); 
        s.setPriceUnit("LẦN");
        s.setFixedPrice(BigDecimal.ZERO);
        s.setActive(true);
        s.setShowOnWeb(true);
        s.setSortOrder(0);
        
        model.addAttribute("service", s);
        model.addAttribute("categories", catalogService.getActiveCategories());
        model.addAttribute("isEdit", false);
        return "admin/service-detail";
    }

    // Trang tạo Combo (Phải có hàm này để tránh lỗi 400)
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
        model.addAttribute("categories", catalogService.getActiveCategories());
        model.addAttribute("singleServices", catalogService.getServices("SINGLE", 1, 100).getContent());
        
        return "admin/combo-create";
    }

    // Xử lý lưu Combo
    @PostMapping("/save-combo")
    public String saveCombo(@ModelAttribute("service") Services combo,
                            @RequestParam("imageFile") MultipartFile imageFile,
                            @RequestParam(value = "childIds", required = false) List<Integer> childIds,
                            RedirectAttributes ra) {
        try {
            catalogService.createCombo(combo, imageFile, childIds);
            ra.addFlashAttribute("message", "Đã tạo Combo thành công.");
            return "redirect:/admin/services?tab=COMBO";
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/services/new-combo";
        }
    }

    // =========================================================
    // 3. TRANG CHI TIẾT / SỬA (URL ĐỘNG)
    // =========================================================

    // Regex chỉ nhận số để an toàn tuyệt đối
    @GetMapping("/{id:\\d+}")
    public String editService(@PathVariable("id") Integer id, Model model, Principal principal) {
        addCommonHeader(model, principal);
        try {
            Services s = catalogService.getServiceById(id);
            model.addAttribute("service", s);
            model.addAttribute("categories", catalogService.getActiveCategories());
            model.addAttribute("isEdit", true);
            
            // Điều hướng sang trang sửa tương ứng
            if ("COMBO".equals(s.getServiceType()) || s.isCombo()) {
                model.addAttribute("singleServices", catalogService.getServices("SINGLE", 1, 100).getContent());
                // TODO: Load checked items (logic view xử lý)
                return "admin/combo-create";
            }
            return "admin/service-detail";
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
            model.addAttribute("categories", catalogService.getActiveCategories());
            model.addAttribute("isEdit", service.getServiceId() != null);
            return "admin/service-detail";
        }
        try {
            catalogService.saveService(service, imageFile);
            ra.addFlashAttribute("message", "Đã lưu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // =========================================================
    // 4. ACTIONS (DELETE, DUPLICATE...)
    // =========================================================
    
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id, RedirectAttributes ra) {
        try { 
            catalogService.deleteService(id); 
            ra.addFlashAttribute("message", "Đã xóa."); 
        } catch (Exception e) { 
            ra.addFlashAttribute("error", "Không thể xóa (Dịch vụ đang được sử dụng)."); 
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
    // 5. AJAX APIS (MATRIX & DATA)
    // =========================================================

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Page<Services>> getServicesApi(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "type", defaultValue = "ALL") String type) {
        return ResponseEntity.ok(catalogService.getServices(type, page, size));
    }

    @GetMapping("/api/services-by-category")
    @ResponseBody
    public ResponseEntity<List<Services>> getServicesByCat(@RequestParam("id") Integer catId) {
        return ResponseEntity.ok(catalogService.getServicesByCategory(catId));
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
    // 6. QUẢN LÝ DANH MỤC (CATEGORY)
    // =========================================================
    
    @GetMapping("/categories")
    public String categories(@RequestParam(name = "keyword", required = false) String keyword, 
                             Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("categories", catalogService.getCategories(keyword));
        model.addAttribute("serviceCounts", catalogService.getCategoryCounts());
        model.addAttribute("keyword", keyword);
        return "admin/categories";
    }
    
    @PostMapping("/categories/save")
    public String saveCategory(ServiceCategory cat, RedirectAttributes ra) {
        try {
            catalogService.saveCategory(cat);
            ra.addFlashAttribute("message", "Đã lưu danh mục thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
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
}