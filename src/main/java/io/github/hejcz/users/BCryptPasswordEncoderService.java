package io.github.hejcz.users;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.NotBlank;

@Singleton
class BCryptPasswordEncoderService implements PasswordEncoder {

    private final PasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(@NotBlank @NonNull CharSequence rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(@NotBlank @NonNull CharSequence rawPassword,
                    @NotBlank @NonNull String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}