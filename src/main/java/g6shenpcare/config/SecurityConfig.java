package g6shenpcare.config;

import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.entity.UserAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                String role = auth.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    response.sendRedirect("/admin/dashboard");
                    return;
                } else if (role.equals("ROLE_DOCTOR")) {
                    response.sendRedirect("/doctor/dashboard");
                    return;
                }
            }
            response.sendRedirect("/");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UserDetailsService userDetailsService(UserAccountRepository userRepo) {
        return username -> {
            UserAccount u = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (!u.isActive()) {
                throw new LockedException("Account is inactive");
            }
            return User.withUsername(u.getUsername())
                    .password(u.getPasswordHash())
                    .roles(u.getRole())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/img/**", "/js/**", "/", "/index.html").permitAll()
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/admin/login", "/admin/register").permitAll()
                // Cho phép cả ADMIN và DOCTOR truy cập /admin/**
                .requestMatchers("/admin/**").hasAnyRole("ADMIN","DOCTOR","GROOMER","SUPPORT","STORE")
                // Cho phép cả DOCTOR và ADMIN truy cập /doctor/**
                .requestMatchers("/doctor/**").hasAnyRole("DOCTOR","ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .successHandler(customSuccessHandler())
                .failureUrl("/admin/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout=true")
            );
        return http.build();
    }
}