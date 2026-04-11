package com.campusly.campusly_backend.actors.user.interfaces.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyOtpRequest {
    private String email;
    private String otpCode;
}
