package com.campusly.campusly_backend.shared.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Value("${app.security.oauth2.frontend-redirect-url:}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        UUID userId = UUID.nameUUIDFromBytes(email.getBytes());
        String role = "STUDENT";

        String token = jwtService.generateToken(userId, email, role);

        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtService.getTokenMaxAgeSeconds());
        cookie.setSecure(jwtService.isSecure());
        cookie.setAttribute("SameSite", jwtService.getSameSiteAttribute());
        response.addCookie(cookie);

        log.info("Login OAuth2 riuscito per utente: {} ({})", name, email);

        if (frontendRedirectUrl != null && !frontendRedirectUrl.isBlank()) {
            response.sendRedirect(frontendRedirectUrl);
        }
    }
}
