package io.github.hejcz.users;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@Singleton
public class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoder passwordEncoder;

    @Override
    public Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest,
                                                          AuthenticationRequest<?, ?> authenticationRequest) {
        return userRepository.findByUsername(authenticationRequest.getIdentity().toString())
            .filter(User::isConfirmed)
            .handle((user, sink) -> {
                if (passwordEncoder.matches(authenticationRequest.getSecret().toString(), user.getPassword())) {
                    sink.next(AuthenticationResponse.success((String) authenticationRequest.getIdentity()));
                    sink.complete();
                } else {
                    sink.error(AuthenticationResponse.exception());
                }
            });
    }
}
