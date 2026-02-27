package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF для API (JWT защищает от CSRF)
                .csrf(csrf -> csrf.disable())

                // Настройка авторизации запросов
                .authorizeHttpRequests(auth -> auth
                        // JWT аутентификация и регистрация - публичные
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/error").permitAll()

                        // 🔥 ИСПРАВЛЕНИЕ: GET запросы к customers доступны USER и ADMIN
                        .requestMatchers(HttpMethod.GET,
                                "/accounts/**",
                                "/transactions/**",
                                "/cards/**",
                                "/deposits/**",
                                "/loans/**",
                                "/exchange/**",
                                "/customers/**").hasAnyRole("USER", "ADMIN")  // 👈 ДОБАВИЛ customers

                        // USER операции (создание/действия)
                        .requestMatchers(HttpMethod.POST,
                                "/transactions/**",
                                "/deposits/open",
                                "/loans/take",
                                "/exchange/convert").hasAnyRole("USER", "ADMIN")

                        // ADMIN только endpoints (полный доступ)
                        .requestMatchers(HttpMethod.POST,
                                "/accounts/**",
                                "/cards/**",
                                "/customers/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasRole("ADMIN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Добавляем JWT фильтр
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Статусные сессии (JWT самодостаточен)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}