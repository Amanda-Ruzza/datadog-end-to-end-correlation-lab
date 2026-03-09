package com.classicjazz.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Component
public class BackendClient {

    private final WebClient webClient;

    public BackendClient(@Value("${backend.url:http://localhost:8000}") String backendUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(backendUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public SignupSigninResponse signup(String firstName, String lastName, String email) {
        return post("/auth/signup", Map.of(
                "first_name", firstName,
                "last_name", lastName,
                "email", email
        ), SignupSigninResponse.class);
    }

    public SignupSigninResponse signin(String email) {
        return post("/auth/signin", Map.of("email", email), SignupSigninResponse.class);
    }

    public CustomerResponse getCustomer(String customerId) {
        return get("/customers/" + customerId, CustomerResponse.class);
    }

    public CheckoutResponse checkout(String firstName, String lastName, String email,
                                     String customerId, java.util.List<Map<String, Object>> items) {
        Map<String, Object> body = new java.util.HashMap<>(Map.of(
                "customer", Map.of(
                        "first_name", firstName,
                        "last_name", lastName,
                        "email", email
                ),
                "items", items
        ));
        if (customerId != null && !customerId.isBlank()) {
            body.put("customer_id", customerId);
        }
        return post("/orders/checkout", body, CheckoutResponse.class);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        try {
            return webClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BackendException(e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }

    private <T> T get(String path, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(path)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException e) {
            throw new BackendException(e.getStatusCode().value(), e.getResponseBodyAsString());
        }
    }

    public record SignupSigninResponse(boolean success, String customer_id, String message) {}
    public record CheckoutResponse(boolean success, String order_id, String message) {}
    public record CustomerResponse(String first_name, String last_name, String email) {}

    public static class BackendException extends RuntimeException {
        private final int statusCode;
        private final String body;

        public BackendException(int statusCode, String body) {
            super("Backend returned " + statusCode + ": " + body);
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
    }
}
