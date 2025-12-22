package g6shenpcare.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
@Primary
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // 1. Ưu tiên check DOCTOR trước (để vào Dashboard riêng)
        if (roles.contains("ROLE_DOCTOR")) {
            response.sendRedirect("/doctor/dashboard");
        } 
        // 2. Check ADMIN và các STAFF quản lý khác (vào Admin Dashboard)
        else if (roles.contains("ROLE_ADMIN") || 
                 roles.contains("ROLE_GROOMER") || 
                 roles.contains("ROLE_STAFF")) {
            response.sendRedirect("/admin/dashboard");
        } 
        // 3. Khách hàng (về trang chủ hoặc trang My Account)
        else if (roles.contains("ROLE_CUSTOMER")) {
            response.sendRedirect("/"); 
        } 
        // 4. Mặc định
        else {
            response.sendRedirect("/");
        }
    }
}