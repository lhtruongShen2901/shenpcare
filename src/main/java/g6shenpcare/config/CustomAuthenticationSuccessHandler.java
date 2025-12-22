package g6shenpcare.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component; // <--- QUAN TRỌNG NHẤT

import java.io.IOException;
import java.util.Set;

@Component // <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY THÌ LỖI MỚI HẾT
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_DOCTOR")) {
            response.sendRedirect("/doctor/dashboard");
        } else if (roles.contains("ROLE_CUSTOMER")) {
            response.sendRedirect("/"); // Khách hàng về trang chủ
        } else {
            response.sendRedirect("/"); // Mặc định
        }
    }
}