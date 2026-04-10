package com.campusly.campusly_backend.actors.user.interfaces.auth;

import java.util.UUID;

import com.campusly.campusly_backend.database.entity.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private UUID id;
    private String username;
    private String email;
    private Role role;

    public enum Role {
        CAMPUSLY_USER,
        CAMPUSLY_CREATOR,
        CAMPUSLY_MODERATOR,
        SUPER_ADMIN
    }

    public static LoginResponse fromUser(User user) {
        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Role.valueOf(user.getRole().name())
        );
    }
}
