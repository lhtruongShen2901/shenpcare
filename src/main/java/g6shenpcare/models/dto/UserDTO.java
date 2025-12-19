package g6shenpcare.models.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Integer userId;
    private String username;
    private String fullName;
    private String role;
}

