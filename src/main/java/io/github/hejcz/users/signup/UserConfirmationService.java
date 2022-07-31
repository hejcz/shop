package io.github.hejcz.users.signup;

import io.github.hejcz.email.EmailSender;
import io.micronaut.context.annotation.Value;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ExecutorService;

@Singleton
public class UserConfirmationService {

    private final EmailSender emailSender;
    private final String hostname;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final Scheduler scheduler;

    @Inject
    public UserConfirmationService(EmailSender emailSender, @Value("${hostname}") String hostname,
                                   JwtTokenGenerator jwtTokenGenerator, @Named(UserConfirmationExecutor.NAME) ExecutorService executorService) {
        this.emailSender = emailSender;
        this.hostname = hostname;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.scheduler = Schedulers.fromExecutor(executorService);
    }

    public Mono<String> sendConfirmationEmail(String email) {
        String token = jwtTokenGenerator.generateToken(Map.of("email", email)).orElseThrow();
        return Mono.just(token)
            .publishOn(scheduler)
            .doOnNext(tk -> emailSender.sendConfirmationLink(email, hostname + "/confirmation/" + tk));
    }

}
