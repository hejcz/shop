package io.github.hejcz.email;

import io.micronaut.email.Contact;
import io.micronaut.email.Email;
import io.micronaut.email.javamail.sender.JavaxEmailSender;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EmailSender {

    private final JavaxEmailSender javaMailSender;

    @Inject
    public EmailSender(JavaxEmailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendConfirmationLink(String toEmail, String confirmationUrl) {
        final String subject = "Aktywuj konto na grupie Gollum";
        final String body = """
            W celu aktywacji swojego konta na grupie Gollum przejdź na: %s
            """.formatted(confirmationUrl);
        Email email = createAnEmail(toEmail, subject, body);
        javaMailSender.send(email);
    }

    private Email createAnEmail(String toEmail, String subject, String body) {
        return Email.builder()
            .to(new Contact(toEmail, "Grupa Gollum"))
            .from(new Contact("grupa.gollum@op.pl"))
            .body(body)
            .subject(subject)
            .build();
    }

}