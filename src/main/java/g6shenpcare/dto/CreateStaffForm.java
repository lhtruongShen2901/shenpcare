package g6shenpcare.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateStaffForm {
    @NotBlank
    private String username;
    
    @NotBlank
    private String fullName;
    
    @Email
    private String email;
    
    @NotBlank
    private String phone;
    
    // Admin chọn Role cho nhân viên (DOCTOR, GROOMER, STAFF...)
    @NotBlank 
    private String role; 
    
    // Có thể set mật khẩu mặc định hoặc random, không cần confirm
    // private String password; 
}