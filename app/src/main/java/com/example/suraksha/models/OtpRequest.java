package com.example.suraksha.models;

public class OtpRequest {
    private String mobile;
    private String otp; // optional for verification

    public OtpRequest(String mobile) {
        this.mobile = mobile;
    }

    public OtpRequest(String mobile, String otp) {
        this.mobile = mobile;
        this.otp = otp;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
