package g6shenpcare.utils;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        for (GrantedAuthority authority : authentication.getAuthorities()) {

            String role = authority.getAuthority(); // ví dụ: ROLE_SUPPORT

            if ("ROLE_SUPPORT".equals(role)) {
                response.sendRedirect("/support/chat");
                return;
            }

            if ("ROLE_GROOMER".equals(role)) {
                response.sendRedirect("/groomer/schedule");
                return;
            }

            if ("ROLE_ADMIN".equals(role)
                    || "ROLE_DOCTOR".equals(role)
                    || "ROLE_STORE".equals(role)) {

                response.sendRedirect("/admin/dashboard");
                return;
            }
        }

        // fallback
        response.sendRedirect("/admin/login?error=true");
    }
}
