package io.github.hejcz.users.signup;

import com.nimbusds.jwt.JWTClaimNames;
import io.github.hejcz.email.EmailSender;
import io.github.hejcz.users.User;
import io.github.hejcz.users.UserRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator;
import io.micronaut.security.token.jwt.generator.claims.ClaimsGenerator;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Singleton
public class UserConfirmationService {

    private final EmailSender emailSender;
    private final String hostname;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final CustomJwtTokenValidator jwtValidator;
    private final ClaimsGenerator claimsGenerator;
    private final Scheduler scheduler;
    private final UserRepository userRepository;
    private final String appName;

    @Inject
    public UserConfirmationService(EmailSender emailSender, @Value("${hostname}") String hostname,
                                   JwtTokenGenerator jwtTokenGenerator, CustomJwtTokenValidator jwtValidator,
                                   ClaimsGenerator claimsGenerator,
                                   @Named(UserConfirmationExecutor.NAME) ExecutorService executorService,
                                   UserRepository userRepository,
                                   ApplicationConfiguration applicationConfiguration) {
        this.emailSender = emailSender;
        this.hostname = hostname;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtValidator = jwtValidator;
        this.claimsGenerator = claimsGenerator;
        this.scheduler = Schedulers.fromExecutor(executorService);
        this.userRepository = userRepository;
        this.appName = applicationConfiguration.getName().orElseThrow();
    }

    public Mono<String> sendConfirmationEmail(String email) {
        Map<String, Object> claimsSet = claimsGenerator.generateClaimsSet(
            Map.of(JWTClaimNames.ISSUER, appName, JWTClaimNames.SUBJECT, "confirm " + email), null);
        return Mono.just(jwtTokenGenerator.generateToken(claimsSet).orElseThrow())
            .publishOn(scheduler)
            .doOnNext(token -> emailSender.sendConfirmationLink(email, hostname + "/confirmation/" + token));
    }

    public Mono<User> confirmEmailToken(String confirmationToken) {
        return Mono.justOrEmpty(jwtValidator.validate(confirmationToken))
            .map(jwt -> {
                try {
                    return jwt.getJWTClaimsSet().getSubject();
                } catch (ParseException e) {
                    // TODO: what is reactor way of raising exceptions?
                    throw new RuntimeException(e);
                }
            })
            .filter(subject -> subject.startsWith("confirm "))
            .map(subject -> subject.split("confirm ")[1])
            .flatMap(userRepository::findByEmail)
            .doOnNext(user -> user.setConfirmed(true))
            .flatMap(userRepository::update);
    }
}
