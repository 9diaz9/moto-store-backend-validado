package com.example.motostore.config;

import com.example.motostore.security.CustomAuthenticationSuccessHandler;
import com.example.motostore.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomUserDetailsService customUserDetailsService) {
        this.successHandler = successHandler;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                        // páginas públicas
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/verify",
                                "/test-email",   // 👈 endpoint de prueba de correo
                                "/api/motos/**",
                                "index"
                        ).permitAll()

                        // solo SUPER_ADMIN puede entrar a /admin/**
                        .requestMatchers("/admin/**").hasRole("SUPER_ADMIN")

                        // cualquier otra ruta requiere login
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")   // usamos email como username
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                )
                .userDetailsService(customUserDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
