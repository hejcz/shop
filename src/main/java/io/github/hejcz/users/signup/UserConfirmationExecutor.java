package io.github.hejcz.users.signup;

import io.micronaut.context.annotation.Factory;
import io.micronaut.scheduling.executor.ExecutorConfiguration;
import io.micronaut.scheduling.executor.ExecutorType;
import io.micronaut.scheduling.executor.UserExecutorConfiguration;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Separate thread pool, so it doesn't block netty threads.
 */
@Factory
public class UserConfirmationExecutor {

    public static final String NAME = "USER_CONFIRMATION";

    @Singleton
    @Named(UserConfirmationExecutor.NAME)
    public ExecutorConfiguration configuration() {
        UserExecutorConfiguration configuration = UserExecutorConfiguration.of(ExecutorType.FIXED, 4);
        configuration.setName(NAME);
        return configuration;
    }
}

