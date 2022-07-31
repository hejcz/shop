package io.github.hejcz.users.signup;

import io.github.hejcz.users.UserRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/sign-up")
public class SignUpController {

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordEncoder passwordEncoder;

    @Post
    public Mono<MutableHttpResponse<Object>> signUp(@Valid @Body SignupForm signupForm, HttpRequest<?> request) {
        return userRepository.findAllByEmailOrUsername(signupForm.email(), signupForm.username())
            .singleOrEmpty()
            .map(user -> HttpResponse.badRequest())
            .switchIfEmpty(userRepository.save(SignupFormMapper.INSTANCE.map(signupForm.withEncodedPassword(passwordEncoder)))
                .map(user -> HttpResponse.created(URI.create("/users/" + user.getId()))));
    }

}
