package io.github.hejcz.users.signup;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import reactor.core.publisher.Mono;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller
public class ConfirmEmailController {

    @Inject
    UserConfirmationService userConfirmationService;

    @Get("/confirmation/{token}")
    public Mono<?> confirmEmailToken(String token) {
        return userConfirmationService.confirmEmailToken(token)
            .map(authentication -> HttpResponse.accepted())
            .switchIfEmpty(Mono.just(HttpResponse.badRequest()));
    }

}
