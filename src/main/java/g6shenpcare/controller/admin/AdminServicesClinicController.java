package g6shenpcare.controller.admin;

import g6shenpcare.entity.ServiceCategory;
import g6shenpcare.entity.Services;
import g6shenpcare.service.ClinicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/admin/services")
public class AdminServicesClinicController {

    private final ClinicService clinicService;

    public AdminServicesClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    private void addCommonHeader(Model model, Principal principal) {
        String username = (principal != null) ? principal.getName() : "admin";
        model.addAttribute("currentUser", username);
        model.addAttribute("clinicName", "ShenPCare Clinic");
        model.addAttribute("activeMenu", "services");
    }

    // LIST
    @GetMapping
    public String listServices(Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Dịch vụ");
        model.addAttribute("services", clinicService.getAllServices());
        return "admin/services";
    }

    // FORM NEW
    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Thêm Dịch vụ mới");
        model.addAttribute("service", new Services());
        model.addAttribute("categories", clinicService.getActiveCategories());
        model.addAttribute("isEdit", false);
        return "admin/service-detail";
    }

    // FORM EDIT
    @GetMapping("/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Sửa Dịch vụ");
        model.addAttribute("service", clinicService.getServiceById(id));
        model.addAttribute("categories", clinicService.getActiveCategories());
        model.addAttribute("isEdit", true);
        return "admin/service-detail";
    }

    // SAVE
    @PostMapping("/save")
    public String saveService(@ModelAttribute("service") Services service, RedirectAttributes ra) {
        clinicService.saveService(service);
        ra.addFlashAttribute("message", "Đã lưu dịch vụ thành công!");
        return "redirect:/admin/services";
    }
    
    // TOGGLE STATUS
    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable("id") Integer id, RedirectAttributes ra) {
        clinicService.toggleServiceStatus(id);
        ra.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        return "redirect:/admin/services";
    }
    // --- SECTION: CATEGORIES NÂNG CAO ---

    @GetMapping("/categories")
    public String listCategories(Model model, Principal principal) {
        addCommonHeader(model, principal);
        model.addAttribute("pageTitle", "Quản lý Nhóm Dịch vụ");
        model.addAttribute("categories", clinicService.getAllCategories());
        // Trả về view mới xịn hơn
        return "admin/categories"; 
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute ServiceCategory category, RedirectAttributes ra) {
        clinicService.saveCategory(category);
        ra.addFlashAttribute("message", "Đã lưu thông tin danh mục.");
        return "redirect:/admin/services/categories";
    }

    // Endpoint xử lý xóa/khóa với tùy chọn
    @PostMapping("/categories/delete")
    public String deleteCategoryProcess(
            @RequestParam("id") Integer catId,
            @RequestParam("action") String action, // "JUST_DELETE", "DISABLE_SERVICES", "MOVE_SERVICES"
            @RequestParam(value = "targetId", required = false) Integer targetCatId,
            RedirectAttributes ra) {

        if ("JUST_DELETE".equals(action)) {
            // Trường hợp danh mục rỗng -> chỉ cần khóa nó lại
            clinicService.deleteCategoryWithOption(catId, null, null);
            ra.addFlashAttribute("message", "Đã khóa danh mục (danh mục này không có dịch vụ con).");
        } 
        else if ("DISABLE_SERVICES".equals(action)) {
            clinicService.deleteCategoryWithOption(catId, "DISABLE_SERVICES", null);
            ra.addFlashAttribute("message", "Đã khóa danh mục và ngưng hoạt động các dịch vụ trực thuộc.");
        } 
        else if ("MOVE_SERVICES".equals(action)) {
            if (targetCatId == null || targetCatId.equals(catId)) {
                ra.addFlashAttribute("error", "Danh mục đích không hợp lệ.");
                return "redirect:/admin/services/categories";
            }
            clinicService.deleteCategoryWithOption(catId, "MOVE_SERVICES", targetCatId);
            ra.addFlashAttribute("message", "Đã chuyển dịch vụ sang danh mục mới và khóa danh mục cũ.");
        }

        return "redirect:/admin/services/categories";
    }
    
    // Mở khóa lại (đơn giản)
    @PostMapping("/categories/{id}/activate")
    public String activateCategory(@PathVariable("id") Integer id, RedirectAttributes ra) {
        clinicService.activateCategory(id);
        ra.addFlashAttribute("message", "Đã kích hoạt lại danh mục.");
        return "redirect:/admin/services/categories";
    }
}