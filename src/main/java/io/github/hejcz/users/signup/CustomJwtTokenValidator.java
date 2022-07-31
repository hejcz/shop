package io.github.hejcz.users.signup;

import com.nimbusds.jwt.JWT;
import io.micronaut.security.token.jwt.encryption.EncryptionConfiguration;
import io.micronaut.security.token.jwt.signature.SignatureConfiguration;
import io.micronaut.security.token.jwt.validator.JwtValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collection;
import java.util.Optional;

/**
 * Based on JwtTokenValidator
 */
@Singleton
public class CustomJwtTokenValidator {

    private final JwtValidator validator;

    /**
     * Constructor.
     *
     * @param signatureConfigurations  List of Signature configurations which are used to attempt validation.
     * @param encryptionConfigurations List of Encryption configurations which are used to attempt validation.
     */
    @Inject
    public CustomJwtTokenValidator(Collection<SignatureConfiguration> signatureConfigurations,
                                   Collection<EncryptionConfiguration> encryptionConfigurations) {
        this.validator = JwtValidator.builder()
            .withSignatures(signatureConfigurations)
            .withEncryptions(encryptionConfigurations)
            .build();
    }

    public Optional<JWT> validate(String token) {
        return validator.validate(token, null);
    }
}