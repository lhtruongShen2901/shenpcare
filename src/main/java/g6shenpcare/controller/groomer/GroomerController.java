package g6shenpcare.controller.groomer;

import g6shenpcare.entity.UserAccount;
import g6shenpcare.models.dto.GroomerPetInfoDTO;
import g6shenpcare.models.dto.GroomerScheduleDTO;
import g6shenpcare.models.dto.GroomingProgressRequest;
import g6shenpcare.models.entity.GroomingBoardingProgress;
import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.service.GroomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/groomer")
public class GroomerController {

    @Autowired
    private GroomerService groomerService;

    @Autowired
    private UserAccountRepository userRepository;


    @GetMapping("/schedule")
    public String viewSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "day") String view,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        UserAccount customer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate selectedDate = date != null ? date : LocalDate.now();

        List<GroomerScheduleDTO> schedules;
        LocalDate startDate, endDate;

        if ("week".equals(view)) {
            WeekFields weekFields = WeekFields.of(Locale.getDefault());
            startDate = selectedDate.with(weekFields.dayOfWeek(), 1);
            endDate = startDate.plusDays(6);
        } else {
            startDate = selectedDate;
            endDate = selectedDate;
        }

        schedules = groomerService.getGroomerSchedule(
                customer.getUserId(), startDate, endDate, serviceType, search
        );

        long totalBookings = schedules.size();
        long pendingBookings = schedules.stream()
                .filter(s -> "PENDING".equals(s.getStatus()) || "CONFIRMED".equals(s.getStatus()))
                .count();
        long inProgressBookings = schedules.stream()
                .filter(s -> "IN_PROGRESS".equals(s.getStatus()))
                .count();
        long completedBookings = schedules.stream()
                .filter(s -> "COMPLETED".equals(s.getStatus()))
                .count();

        model.addAttribute("schedules", schedules);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("view", view);
        model.addAttribute("serviceType", serviceType);
        model.addAttribute("search", search);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("inProgressBookings", inProgressBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("staffName", userDetails.getUsername());

        return "groomer/schedule-spa-boarding";
    }

    @PostMapping("/schedule/{bookingId}/status")
    @ResponseBody
    public String updateBookingStatus(
            @PathVariable Integer bookingId,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserAccount customer = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        groomerService.updateBookingStatus(bookingId, customer.getUserId(), status, notes);
        return "success";
    }

    @GetMapping("/{bookingId}/pet-info")
    public String viewPetInfo(
            @PathVariable Integer bookingId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            GroomerPetInfoDTO petInfo = groomerService.getPetInfoForGrooming(bookingId);
            boolean isAssignedToGroomer = groomerService.isAssignedToGroomer(bookingId, userDetails.getUsername());

            if (!isAssignedToGroomer) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bạn không có quyền truy cập booking này!");
                return "redirect:/groomer/schedule";
            }

            // Lấy danh sách progress history
            List<GroomingBoardingProgress> progressHistory = groomerService.getProgressHistory(bookingId);

            model.addAttribute("booking", petInfo.getBooking());
            model.addAttribute("pet", petInfo.getPet());
            model.addAttribute("customer", petInfo.getCustomer());
            model.addAttribute("service", petInfo.getService());
            model.addAttribute("petAge", petInfo.getPetAge());
            model.addAttribute("staffName", userDetails.getUsername());
            model.addAttribute("previousVisits", petInfo.getPreviousVisits());
            model.addAttribute("progressHistory", progressHistory); // Thêm lịch sử tiến độ

            return "groomer/pet-info";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể tải thông tin thú cưng: " + e.getMessage());
            return "redirect:/groomer/schedule";
        }
    }

    @PostMapping("/{bookingId}/progress")
    public String updateProgress(
            @PathVariable Integer bookingId,
            @ModelAttribute GroomingProgressRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            if (!groomerService.isAssignedToGroomer(bookingId, userDetails.getUsername())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bạn không có quyền cập nhật booking này!");
                return "redirect:/groomer/schedule";
            }

            String action = request.getAction();

            if (request.getNotes() == null || request.getNotes().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Vui lòng nhập ghi chú tiến độ!");
                return "redirect:/groomer/" + bookingId + "/pet-info";
            }

            if ("complete".equals(action)) {
                groomerService.completeGrooming(bookingId, request, userDetails.getUsername());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã hoàn thành grooming cho thú cưng!");
                return "redirect:/groomer/schedule";
            } else {
                groomerService.updateGroomingProgress(bookingId, request, userDetails.getUsername());
                redirectAttributes.addFlashAttribute("successMessage",
                        "Đã cập nhật tiến độ grooming!");
                return "redirect:/groomer/" + bookingId + "/pet-info";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể cập nhật tiến độ: " + e.getMessage());
            return "redirect:/groomer/" + bookingId + "/pet-info";
        }
    }
}