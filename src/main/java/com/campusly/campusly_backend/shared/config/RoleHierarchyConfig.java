package com.campusly.campusly_backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Gerarchia dei ruoli Campusly.
 *
 *   SUPER_ADMIN
 *       └─► CAMPUSLY_MODERATOR
 *               ├─► CAMPUSLY_USER     (utente standard dell'app)
 *               └─► CAMPUSLY_CREATOR  (stesso livello di USER, può creare eventi)
 *
 * USER e CREATOR sono peer: accedono agli stessi endpoint.
 * La differenza è solo funzionale: CREATOR può creare eventi, USER no.
 * MODERATOR (e SUPER_ADMIN) eredita entrambi i ruoli.
 */
@Configuration
@EnableMethodSecurity
public class RoleHierarchyConfig {

    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("SUPER_ADMIN").implies("CAMPUSLY_MODERATOR")
                .role("CAMPUSLY_MODERATOR").implies("CAMPUSLY_USER")
                .role("CAMPUSLY_MODERATOR").implies("CAMPUSLY_CREATOR")
                .build();
    }

}
