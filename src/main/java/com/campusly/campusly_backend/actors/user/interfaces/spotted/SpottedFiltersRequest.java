package com.campusly.campusly_backend.actors.user.interfaces.spotted;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpottedFiltersRequest {
    private UUID universityId;

    public void isValid(String errorTitle) throws ExceptionBackend {
        if (universityId == null) {
            throw ExceptionBackend.fromError(errorTitle,
                    "L'ID università è obbligatorio. CODICE: SP001", null, HttpStatus.BAD_REQUEST);
        }
    } 
}
