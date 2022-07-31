package io.github.hejcz;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class BoardGamesStoreTest implements TestPropertyProvider {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:5.0.9")
        .withExposedPorts(27017);

    static {
        mongoDBContainer.start();
    }

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void shouldHaveAuthorizationConfigured() {
        Assertions.assertThatThrownBy(() ->
                httpClient.toBlocking().exchange(HttpRequest.GET("/")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class))
            .hasMessage("Unauthorized");
    }

    @Test
    void shouldBeAbleToBypassAuthorizationWithAccessToken() {
        HttpResponse<String> signUpResponse = httpClient.toBlocking().exchange(HttpRequest.POST("/sign-up", """
                {
                    "username": "sherlock",
                    "password": "password123",
                    "email": "sherlock-holmes@gmail.com",
                    "firstName": "Sherlock",
                    "lastName": "Holmes"
                }
                """)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class);
        Assertions.assertThat(signUpResponse.code()).isEqualTo(201);

        // existing e-mail
        Assertions.assertThatThrownBy(() ->
            httpClient.toBlocking().exchange(HttpRequest.POST("/sign-up", """
                {
                    "username": "sherlock12",
                    "password": "password123",
                    "email": "sherlock-holmes@gmail.com",
                    "firstName": "Sherlock",
                    "lastName": "Holmes"
                }
                """)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class))
            .hasMessage("Bad Request");

        // existing username
        Assertions.assertThatThrownBy(() ->
                httpClient.toBlocking().exchange(HttpRequest.POST("/sign-up", """
                {
                    "username": "sherlock",
                    "password": "password123",
                    "email": "sherlock-holmes-2@gmail.com",
                    "firstName": "Sherlock",
                    "lastName": "Holmes"
                }
                """)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class))
            .hasMessage("Bad Request");

        HttpResponse<String> anotherSignUpResponse = httpClient.toBlocking().exchange(HttpRequest.POST("/sign-up", """
                {
                    "username": "sherlock12",
                    "password": "password123",
                    "email": "sherlock-holmes-12@gmail.com",
                    "firstName": "Sherlock",
                    "lastName": "Holmes"
                }
                """)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class);
        Assertions.assertThat(anotherSignUpResponse.code()).isEqualTo(201);

        HttpResponse<LoginResponse> loginResponse = httpClient.toBlocking().exchange(HttpRequest.POST("/login", """
                {"username":"sherlock", "password":"password123"}
                """)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), LoginResponse.class);
        Assertions.assertThat(loginResponse.body()).isNotNull();

        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET("/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.body().accessToken()), String.class);
        Assertions.assertThat(response.code()).isEqualTo(200);
    }

    @Override
    public @NotNull Map<String, String> getProperties() {
        Integer mappedPort = mongoDBContainer.getMappedPort(27017);
        return Map.of("mongodb.uri", "mongodb://${MONGO_HOST:localhost}:" + mappedPort);
    }
}
