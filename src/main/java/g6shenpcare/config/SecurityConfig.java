package g6shenpcare.config;

import g6shenpcare.repository.UserAccountRepository;
import g6shenpcare.entity.UserAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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
                    .roles(u.getRole())  // ADMIN, DOCTOR, GROOMER, SUPPORT, STORE, CUSTOMER
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. static + trang public + API FILES (QUAN TRỌNG)
                .requestMatchers("/css/**", "/img/**", "/js/**", "/", "/index.html").permitAll()
                .requestMatchers("/api/files/**").permitAll() // <--- Cho phép truy cập ảnh công khai
                // 2. ĐẶT /admin/login, /admin/register TRƯỚC /admin/**
                .requestMatchers("/admin/login", "/admin/register").permitAll()
                // 3. Khu admin nội bộ (Admin + Staff)
                .requestMatchers("/admin/**").hasAnyRole("ADMIN","DOCTOR","GROOMER","SUPPORT","STORE")
                .requestMatchers("/support/**").hasAnyRole("SUPPORT", "ADMIN")
                // 4. Mọi request khác
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
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