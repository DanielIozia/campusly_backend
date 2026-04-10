package com.campusly.campusly_backend.shared.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Invia un'email HTML usando il template base di Campusly.
     *
     * @param to         Indirizzo destinatario
     * @param subject    Oggetto dell'email
     * @param emailTitle Titolo grande visibile nel body dell'email
     * @param emailBody  Testo HTML del corpo (può contenere tag HTML semplici)
     */
    @Async
    public void sendHtmlEmail(String to, String subject, List<String> cc, String emailTitle, String emailBody) {
        try {
            Context context = new Context();
            context.setVariable("emailTitle", emailTitle);
            context.setVariable("emailBody", emailBody);

            String htmlContent = templateEngine.process("email/base-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("campusly.doc@gmail.com", "Campusly");
            helper.setTo("danieliozia.di@gmail.com"); //! da mettere "to"
            helper.setCc(cc.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email inviata a: {}", to, cc.isEmpty() ? "" : " (CC: " + String.join(", ", cc) + ")");

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Errore invio email a {}: {}", to, e.getMessage());
            throw ExceptionBackend.fromError(
                    "Errore invio email",
                    "Impossibile inviare l'email a " + to + (cc.isEmpty() ? "" : " (CC: " + String.join(", ", cc) + ")") + ", CODICE: EM100",
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
