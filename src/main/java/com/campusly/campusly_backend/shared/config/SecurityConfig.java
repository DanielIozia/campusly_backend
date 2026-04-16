package com.campusly.campusly_backend.shared.config;

import com.campusly.campusly_backend.shared.security.AppRoles;
import com.campusly.campusly_backend.shared.security.JwtAccessDeniedHandler;
import com.campusly.campusly_backend.shared.security.JwtAuthenticationEntryPoint;
import com.campusly.campusly_backend.shared.security.JwtAuthenticationFilter;
import com.campusly.campusly_backend.shared.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

/**
 * Configurazione della sicurezza HTTP.
 *
 * Le regole sono organizzate per livello di accesso crescente:
 *   1. PUBLIC       — nessuna autenticazione richiesta
 *   2. USER         — qualsiasi utente autenticato (CAMPUSLY_USER e superiori)
 *   3. CREATOR      — creatori di contenuti (CAMPUSLY_CREATOR e superiori)
 *   4. MODERATOR    — moderatori (CAMPUSLY_MODERATOR e superiori)
 *   5. ADMIN        — solo SUPER_ADMIN
 *
 * SUPER_ADMIN eredita i permessi di tutti i ruoli grazie a RoleHierarchyConfig.
 * I gruppi AppRoles.*_AND_ABOVE rendono esplicita questa ereditarietà anche
 * per versioni di Spring Security precedenti alla 6.3.
 */
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
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(auth -> auth

                        // ── 1. PUBLIC ────────────────────────────────────────
                        .requestMatchers(
                                "/auth/login",
                                "/auth/forgot-password",
                                "/auth/verify-password-otp",
                                "/auth/reset-password",
                                "/register/send-otp",
                                "/register/verify-otp",
                                "/register/resend-otp",
                                "/register/complete"
                        ).permitAll()
                        .requestMatchers("/api-docs/**", "/scalar/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // ── 2. Utenti autenticati (USER = CREATOR) ───────────
                        // Auth
                        .requestMatchers("/auth/me", "/auth/logout").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        // Profilo utente
                        .requestMatchers("/users/me/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        // Università — lettura
                        .requestMatchers(HttpMethod.GET, "/universities/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        // Spotted — lettura e scrittura
                        .requestMatchers(HttpMethod.GET, "/spotted/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        .requestMatchers(HttpMethod.POST, "/spotted").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        .requestMatchers(HttpMethod.PUT, "/spotted/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        .requestMatchers(HttpMethod.DELETE, "/spotted/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)
                        // File
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()
                        // Eventi — lettura aperta a tutti gli utenti
                        .requestMatchers(HttpMethod.GET, "/events/**").hasAnyRole(AppRoles.AUTHENTICATED_USERS)

                        // ── 3. CAMPUSLY_CREATOR (creazione eventi) ────────────
                        .requestMatchers(HttpMethod.POST, "/events").hasAnyRole(AppRoles.EVENT_CREATORS)
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasAnyRole(AppRoles.EVENT_CREATORS)
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasAnyRole(AppRoles.EVENT_CREATORS)

                        // ── 4. CAMPUSLY_MODERATOR ─────────────────────────────
                        .requestMatchers("/moderation/**").hasAnyRole(AppRoles.MODERATORS)

                        // ── 5. SUPER_ADMIN ────────────────────────────────────
                        .requestMatchers("/admin/**").hasAnyRole(AppRoles.ADMIN_ONLY)

                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler))

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
