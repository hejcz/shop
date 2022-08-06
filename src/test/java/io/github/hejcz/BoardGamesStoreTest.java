package io.github.hejcz;

import io.github.hejcz.email.EmailSender;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, MongoFriendlyMicronautTest.class})
class BoardGamesStoreTest {

    @Inject
    HttpClient httpClient;

    @Mock
    EmailSender emailSender;

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

        ArgumentCaptor<String> confirmationUrlCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailSender).sendConfirmationLink(Mockito.anyString(), confirmationUrlCaptor.capture());

        String confirmationToken = confirmationUrlCaptor.getValue()
            .substring(confirmationUrlCaptor.getValue().indexOf("confirmation") + 13);

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

        // before confirmation user can't log
        Assertions.assertThatThrownBy(() ->
                httpClient.toBlocking().exchange(HttpRequest.POST("/login", """
                {"username":"sherlock", "password":"password123"}
                """)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), LoginResponse.class))
            .hasMessage("Unauthorized");

        // confirm user
        HttpResponse<String> confirmationRequest = httpClient.toBlocking()
            .exchange(HttpRequest.GET("/confirmation/" + confirmationToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON), String.class);
        Assertions.assertThat(confirmationRequest.code()).isEqualTo(202);

        // now they can log in
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
}
