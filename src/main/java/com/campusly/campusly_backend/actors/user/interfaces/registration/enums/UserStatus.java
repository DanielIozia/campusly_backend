package com.campusly.campusly_backend.actors.user.interfaces.registration.enums;

/*
    * CODE_VERIFICATION → utente ha inserito l'email e attende il codice OTP
    * SIGNUP            → OTP verificato, ma il profilo non è ancora completo
    * ACTIVE            → registrazione completata, account pienamente operativo
 */
public enum UserStatus {
    CODE_VERIFICATION,
    SIGNUP,
    ACTIVE
}
