package com.campusly.campusly_backend.shared.config;

import com.campusly.campusly_backend.shared.security.JwtAccessDeniedHandler;
import com.campusly.campusly_backend.shared.security.JwtAuthenticationEntryPoint;
import com.campusly.campusly_backend.shared.security.JwtAuthenticationFilter;
import com.campusly.campusly_backend.shared.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Gestione errori di autenticazione/autorizzazione con payload JSON
                // standardizzato
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // Regole di autorizzazione
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/verify-password-otp",
                                "/auth/reset-password",
                                "/register/send-otp",
                                "/register/verify-otp",
                                "/register/resend-otp",
                                "/register/complete")
                        .permitAll()
                        .requestMatchers(
                                "/api-docs/**",
                                "/scalar/**")
                        .permitAll()
                        .requestMatchers(
                                "/actuator/**")
                        .permitAll()
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**")
                        .permitAll()

                        .anyRequest().authenticated())

                // OAuth2 Login (Google)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler))

                // Filtro JWT prima del filtro standard di autenticazione
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
