package com.campusly.campusly_backend.actors.user.interfaces.registration;
import org.springframework.http.HttpStatus;
import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationInitRequest {

    private String email;

    public void isValid() {
        if (email == null || email.isBlank()) {
            throw ExceptionBackend.fromError(
                    "Errore registrazione",
                    "Email obbligatoria. CODICE: AU110",
                    email, HttpStatus.BAD_REQUEST);
        }
    }
}
