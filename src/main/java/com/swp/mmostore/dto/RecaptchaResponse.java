package com.swp.mmostore.dto;

// RecaptchaResponse.java
// We only care about the 'success' field for this
public class RecaptchaResponse {
    private boolean success;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
}