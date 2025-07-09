package com.example.suraksha;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;

import java.util.UUID;

public class SignUp extends AppCompatActivity {
    TextView headingText, subText, tvOtpMessage;
    LinearLayout otpBoxLayout;
    EditText editTextPhone, inputName;
    Button btnSendOtp, btnVerifyOtp;
    EditText[] otpBoxes;
    String backendUrl = "http://172.16.19.12:5000/api/otp";
    final int OTP_BOX_COUNT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
        setContentView(R.layout.activity_signup);

        headingText = findViewById(R.id.headingText);
        subText = findViewById(R.id.subText);
        otpBoxLayout = findViewById(R.id.otpBoxLayout);
        editTextPhone = findViewById(R.id.editTextPhone);
        inputName = findViewById(R.id.inputName);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpMessage = findViewById(R.id.tvOtpMessage);

        otpBoxLayout.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        tvOtpMessage.setVisibility(View.GONE);

        btnSendOtp.setOnClickListener(v -> {
            String mobile = editTextPhone.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            if (mobile.length() == 10) {
                sendOtp(mobile);
            } else {
                Toast.makeText(this, "Enter valid 10-digit number", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = getOtpFromBoxes();
            String mobile = editTextPhone.getText().toString().trim();
            if (otp.length() == 6) {
                verifyOtp(mobile, otp);
            } else {
                Toast.makeText(this, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOtpBoxes(int count) {
        otpBoxLayout.setVisibility(View.VISIBLE);
        otpBoxLayout.removeAllViews();
        otpBoxes = new EditText[count];

        for (int i = 0; i < count; i++) {
            EditText box = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 130);
            params.setMargins(8, 0, 8, 0);
            box.setLayoutParams(params);
            box.setBackgroundResource(R.drawable.box_background);
            box.setInputType(InputType.TYPE_CLASS_NUMBER);
            box.setEms(1);
            box.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            box.setTextSize(18);
            box.setTextColor(Color.BLACK);
            box.setId(View.generateViewId());

            int finalI = i;
            box.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && finalI < count - 1) {
                        otpBoxes[finalI + 1].requestFocus();
                    }
                }
            });

            otpBoxLayout.addView(box);
            otpBoxes[i] = box;
        }

        otpBoxes[0].requestFocus();
    }

    private String getOtpFromBoxes() {
        StringBuilder sb = new StringBuilder();
        for (EditText box : otpBoxes) {
            sb.append(box.getText().toString().trim());
        }
        return sb.toString();
    }

    private void sendOtp(String mobile) {
        try {
            JSONObject params = new JSONObject();
            params.put("mobile", mobile);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    backendUrl + "/send",
                    params,
                    response -> {
                        headingText.setText("Enter OTP");
                        subText.setVisibility(View.GONE);
                        editTextPhone.setVisibility(View.GONE);
                        inputName.setVisibility(View.GONE);
                        findViewById(R.id.nameSubText).setVisibility(View.GONE);

                        createOtpBoxes(OTP_BOX_COUNT);
                        btnVerifyOtp.setVisibility(View.VISIBLE);
                        btnSendOtp.setVisibility(View.GONE);

                        try {
                            String otp = response.getString("otp");
                            tvOtpMessage.setText("Your OTP is: " + otp + " and it is valid for 5 minutes only");
                            tvOtpMessage.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            Toast.makeText(this, "OTP received but can't parse", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "OTP Send Error", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyOtp(String mobile, String otp) {
        try {
            JSONObject params = new JSONObject();
            params.put("mobile", mobile);
            params.put("otp", otp);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    backendUrl + "/verify",
                    params,
                    response -> {
                        Toast.makeText(this, "✅ OTP Verified!", Toast.LENGTH_SHORT).show();

                        // 👉 Generate and save UUID as userID
                        String userID = UUID.randomUUID().toString();
                        String name = inputName.getText().toString().trim();

                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userID", userID);
                        editor.putString("mobile", mobile);
                        editor.putString("name", name);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        new AlertDialog.Builder(SignUp.this)
                                .setTitle("Set Passcode")
                                .setMessage("For your privacy and faster access, please set a secure passcode. You’ll use this passcode to unlock the app next time.\n\nYou can also enable fingerprint login after setting your passcode.")
                                .setCancelable(false)
                                .setPositiveButton("Set Passcode", (dialog, which) -> {
                                    Intent intent = new Intent(SignUp.this, Passcode.class);
                                    intent.putExtra("mobile", mobile);
                                    intent.putExtra("name", name);
                                    intent.putExtra("userID", userID);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                    },
                    error -> {
                        Toast.makeText(this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
