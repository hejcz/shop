package io.github.hejcz.email;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.email.javamail.sender.SessionProvider;
import jakarta.inject.Singleton;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

// https://ustawienia.poczta.onet.pl/Konto/KontoGlowne - enable SMTP
@Singleton
public class OnetSessionProvider implements SessionProvider {

    private final Properties properties = getProperties();

    @Value("${javamail.authentication.username}")
    String username;

    @Value("${javamail.authentication.password}")
    String password;

    @Override
    @NonNull
    public Session session() {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.host", "smtp.poczta.onet.pl");
        properties.setProperty("mail.port", "465");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.ssl.enabled", "true");
        properties.setProperty("mail.smtp.starttls.enabled", "true");
        properties.setProperty("mail.smtp.starttls.required", "true");
        properties.setProperty("mail.smtp.debug","true");
        return properties;
    }
}
