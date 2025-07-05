package com.example.suraksha.models;

public class OtpResponse {
    private String message;
    private String otp; // only for response on sendOtp (for testing purposes)

    public OtpResponse(String message, String otp) {
        this.message = message;
        this.otp = otp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
