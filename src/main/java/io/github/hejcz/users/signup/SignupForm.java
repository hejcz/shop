package io.github.hejcz.users.signup;

import io.micronaut.core.annotation.Introspected;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Introspected
public record SignupForm(
    @NotBlank String username,
    @NotBlank @Size(min = 10) String password,
    @NotBlank @Email String email,
    @NotBlank String firstName,
    @NotBlank String lastName
) {
    SignupForm withEncodedPassword(PasswordEncoder passwordEncoder) {
        return new SignupForm(username, passwordEncoder.encode(password), email, firstName, lastName);
    }
}
