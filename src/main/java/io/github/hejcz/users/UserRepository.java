package io.github.hejcz.users;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@MongoRepository(databaseName = "gollum")
public interface UserRepository extends ReactorCrudRepository<User, String> {

    @NonNull
    Flux<User> findAllByEmailOrUsername(@Nullable String email, @Nullable String username);

    @NonNull
    Mono<User> findByUsername(@Nullable String username);

}
