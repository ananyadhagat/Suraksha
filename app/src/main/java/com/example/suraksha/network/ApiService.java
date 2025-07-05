package com.example.suraksha.network;

import com.example.suraksha.models.OtpRequest;
import com.example.suraksha.models.OtpResponse;  // Corrected to OtpResponse

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/otp/send")
    Call<OtpResponse> sendOtp(@Body OtpRequest request);

    @POST("/api/otp/verify")
    Call<OtpResponse> verifyOtp(@Body OtpRequest request);
}
