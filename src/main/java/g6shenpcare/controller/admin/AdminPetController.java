package g6shenpcare.controller.admin;

import g6shenpcare.entity.Pets;
import g6shenpcare.service.PetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pets")
public class AdminPetController {

    private final PetService petService;

    public AdminPetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping("/save")
    public String savePet(@ModelAttribute Pets pet, RedirectAttributes ra) {
        petService.savePet(pet);
        ra.addFlashAttribute("message", "Đã lưu thông tin thú cưng.");
        // Quay lại trang chi tiết khách hàng
        return "redirect:/admin/customers/" + pet.getCustomerId();
    }

    @PostMapping("/delete")
    public String deletePet(@RequestParam("petId") Integer petId, 
                            @RequestParam("customerId") Integer customerId,
                            RedirectAttributes ra) {
        try {
            petService.deletePet(petId);
            ra.addFlashAttribute("message", "Đã xóa thú cưng.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage()); // Báo lỗi nếu có booking
        }
        return "redirect:/admin/customers/" + customerId;
    }
}