package com.swp.mmostore.service;

// RecaptchaService.java
import com.swp.mmostore.dto.RecaptchaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String secretKey;

    @Value("${google.recaptcha.verify-url}")
    private String verifyUrl;

    private final RestTemplate restTemplate;

    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public boolean validateRecaptcha(String recaptchaResponse) {
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", recaptchaResponse);

        try {
            RecaptchaResponse response = restTemplate.postForObject(verifyUrl, body, RecaptchaResponse.class);
            return response != null && response.isSuccess();
        } catch (Exception e) {
            // Log the error
            return false;
        }
    }
}