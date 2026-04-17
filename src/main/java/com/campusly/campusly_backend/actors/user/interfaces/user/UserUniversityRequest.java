package com.campusly.campusly_backend.actors.user.interfaces.user;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.http.HttpStatus;

import java.util.UUID;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUniversityRequest {
    private UUID universityId;

    public void isValid(String errorTitle) {
        if (universityId == null) {
            throw ExceptionBackend.fromError(errorTitle,
                    "L'ID università è obbligatorio. CODICE: US001", null, HttpStatus.BAD_REQUEST);
        }
    }
}
