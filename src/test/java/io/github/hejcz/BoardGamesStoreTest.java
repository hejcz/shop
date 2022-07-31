package io.github.hejcz;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
class BoardGamesStoreTest {

    @Inject
    EmbeddedApplication<?> application;

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void testItWorks() {
        Assertions.assertThat(application.isRunning()).isTrue();
    }

    @Test
    void shouldHaveAuthorizationConfigured() {
        Assertions.assertThatThrownBy(() ->
                httpClient.toBlocking().exchange(HttpRequest.GET("/")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class))
            .hasMessage("Unauthorized");
    }

    @Test
    void shouldBeAbleToBypassAuthorizationWithAccessToken() {
        HttpResponse<LoginResponse> loginResponse = httpClient.toBlocking().exchange(HttpRequest.POST("/login", """
                {"username":"sherlock", "password":"password"}
                """)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), LoginResponse.class);
        Assertions.assertThat(loginResponse.body()).isNotNull();
        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET("/")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.body().accessToken()), String.class);
        Assertions.assertThat(response.code()).isEqualTo(200);
    }
}
