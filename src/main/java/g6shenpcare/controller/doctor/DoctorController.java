
package g6shenpcare.controller.doctor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    // Show add customer form
    @GetMapping("/customers/new")
    public String newCustomer(Model model) {
        // Provide an empty customer object for the form
        class User {
            public String fullName = "";
            public String email = "";
            public String phone = "";
        }
        class Customer {
            public User user = new User();
            public String city = "";
            public String district = "";
            public String defaultAddress = "";
            public int customerId = 0;
        }
        model.addAttribute("customer", new Customer());
        return "doctor/function/add_customer";
    }

    // Show edit customer form
    @GetMapping("/customers/{id}/edit")
    public String editCustomerById(@org.springframework.web.bind.annotation.PathVariable("id") int id, Model model) {
        // TODO: Load customer by id from DB, here is dummy data for UI
        class User {
            public String fullName = "Nguyễn Văn A";
            public String email = "email@example.com";
            public String phone = "0912345678";
        }
        class Customer {
            public User user = new User();
            public String city = "Hà Nội";
            public String district = "Thanh Xuân";
            public String defaultAddress = "123 Nguyễn Trãi";
            public int customerId = id;
        }
        model.addAttribute("customer", new Customer());
        return "doctor/function/edit_customer";
    }

    @GetMapping("/appointments/new")
    public String newAppointment() {
        return "doctor/function/add_appointment";
    }

    @GetMapping("/settings/register-pin")
    public String settingsRegisterPin() {
        return "doctor/function/register_pin";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        // TODO: Lấy dữ liệu thực tế từ DB, tạm thời truyền các biến rỗng để tránh lỗi null
        model.addAttribute("totalRevenue", 0);
        model.addAttribute("totalCustomers", 0);
        model.addAttribute("totalPets", 0);
        model.addAttribute("appointments", java.util.Collections.emptyList());
        model.addAttribute("chartLabels", java.util.Collections.emptyList());
        model.addAttribute("chartValues", java.util.Collections.emptyList());
        return "doctor/function/reports";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        // Đường dẫn template đúng: templates/doctor/function/bangdieukhien.html
        return "doctor/function/bangdieukhien";
    }

    @GetMapping("/add-appointment")
    public String addAppointment() {
        return "doctor/function/add_appointment";
    }

    @GetMapping("/add-customer-1")
    public String addCustomer1() {
        return "doctor/function/add_customer_1";
    }

    @GetMapping("/add-customer-complete")
    public String addCustomerComplete() {
        return "doctor/function/add_customer_complete";
    }

    @GetMapping("/add-customer-full")
    public String addCustomerFull() {
        return "doctor/function/add_customer_full";
    }

    @GetMapping("/add-customer")
    public String addCustomer() {
        return "doctor/function/add_customer";
    }

    @GetMapping("/baocao")
    public String baocao() {
        return "doctor/function/baocao";
    }

    @GetMapping("/confirm-pin")
    public String confirmPin() {
        return "doctor/function/confirm_pin";
    }

    @GetMapping("/create-pin")
    public String createPin() {
        return "doctor/function/create_pin";
    }

    @GetMapping("/customer-form")
    public String customerForm() {
        return "doctor/function/customer_form";
    }

    @GetMapping("/edit-customer")
    public String editCustomer() {
        return "doctor/function/edit_customer";
    }

    @GetMapping("/exam-form")
    public String examForm() {
        return "doctor/function/exam_form";
    }

    @GetMapping("/khachhang")
    public String khachhang() {
        return "doctor/function/khachhang";
    }

    @GetMapping("/layout")
    public String layout() {
        return "doctor/function/layout";
    }

    @GetMapping("/pet-passport")
    public String petPassport() {
        return "doctor/function/pet_passport";
    }

    @GetMapping("/pin-modal")
    public String pinModal() {
        return "doctor/function/pin_modal";
    }

    @GetMapping("/quanlycongviec")
    public String quanlycongviec() {
        return "doctor/function/quanlycongviec";
    }

    @GetMapping("/register-pin")
    public String registerPin() {
        return "doctor/function/register_pin";
    }

    @GetMapping("/settings")
    public String settings() {
        return "doctor/function/settings";
    }

    @GetMapping("/update-progress")
    public String updateProgress() {
        return "doctor/function/update_progress";
    }

    // Cập nhật đường dẫn đúng cho trang quản lý lịch hẹn và khách hàng
    @GetMapping("/work")
    public String work(Model model) {
        // TODO: Lấy danh sách lịch hẹn thực tế từ DB, tạm thời truyền List rỗng để tránh lỗi null
        model.addAttribute("appointments", java.util.Collections.emptyList());
        model.addAttribute("completedCount", 0);
        model.addAttribute("pendingCount", 0);
        return "doctor/function/work";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        // Dummy data for UI testing
        class User {
            public String fullName = "Nguyễn Văn A";
            public String email = "email@example.com";
            public String phone = "0912345678";
        }
        class Customer {
            public User user = new User();
            public String city = "Hà Nội";
            public String district = "Thanh Xuân";
            public String defaultAddress = "123 Nguyễn Trãi";
            public int customerId = 101;
        }
        java.util.List<Customer> customers = new java.util.ArrayList<>();
        customers.add(new Customer());
        model.addAttribute("customers", customers);
        // Ensure pagination variables are always present
        model.addAttribute("currentPage", 0);
        model.addAttribute("totalPages", 1);
        return "doctor/function/customers";
    }
}
