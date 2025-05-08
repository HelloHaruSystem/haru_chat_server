package com.example.haru.Server.auth;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONObject;

public class TokenValidator {
    //TODO: Move this to .env file
    private final String authServerUrl;
    private final HttpClient httpClient;

    // constants
    private static final int TIMEOUT_SECONDS = 10;

    public TokenValidator(String authServerUrl) {
        this.authServerUrl = authServerUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
    }

    //TODO: remove debug print commands afterIt has been tested
    public ValidateResult validateToken(String username, String token) throws IOException, InterruptedException {
        URI uri = URI.create(this.authServerUrl + "/validate");
        
        // create request body
        JSONObject requestBody = new JSONObject();
        requestBody.put("username", username);

        System.out.println("Request body: " + requestBody.toString()); // debug

        // build the Http request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

        System.out.println("Sending validation request..."); // debug

        // send response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // handle response
        int statusCode = response.statusCode();
        String responseBody = response.body();

        System.out.println("Response Status Code: " + statusCode); // debug
        System.out.println("Response body: " + responseBody);

        try {
            if (statusCode == 200) {
                JSONObject responseJson = new JSONObject(response.body());
                boolean valid = responseJson.getBoolean("valid");
    
                return new ValidateResult(true, "Token validated");
            } else if (statusCode == 401) {
                return new ValidateResult(false, "Invalid or expired token");
            } else {
                return new ValidateResult(false, "Error, status code: " + statusCode);
            }
        } catch (Exception e) {
            System.out.println("Error parsing Json response: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }   

    public static class ValidateResult {
        private final boolean isValid;
        private final String message;

        public ValidateResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean getIsValid() {
            return this.isValid;
        }

        public String getMessage() {
            return this.message;
        }
    }
}
