package com.example.suraksha;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.text.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;

public class Passcode extends AppCompatActivity {
    LinearLayout passcodeLayout, otpLayout;
    Button btnProceed;
    EditText[] passcodeBoxes, otpBoxes;
    TextView subText, tvOtpInstruction;
    String mobile,name, passcode = "";
    final int BOX_COUNT = 6;
    // URLs
    String otpUrl = "http://192.168.1.30:5000/api/otp";
    String userUrl = "http://192.168.1.30:5000/api/user";
    boolean otpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_passcode);

        mobile = getIntent().getStringExtra("mobile");
        name = getIntent().getStringExtra("name");

        passcodeLayout = findViewById(R.id.passcodeBoxLayout);
        otpLayout = findViewById(R.id.otpBoxLayout);
        btnProceed = findViewById(R.id.btnProceed);
        subText = findViewById(R.id.subText);
        tvOtpInstruction = findViewById(R.id.tvOtpInstruction);

        otpLayout.setVisibility(View.GONE);
        tvOtpInstruction.setVisibility(View.GONE);
        subText.setText("Enter passcode");
        subText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

        createBoxes(passcodeLayout, BOX_COUNT, true);

        btnProceed.setOnClickListener(v -> {
            if (!otpSent) {
                passcode = getBoxValue(passcodeBoxes);
                if (passcode.length() == BOX_COUNT) {
                    sendOtpAgain(mobile);

                    // Update UI on first OTP send
                    otpLayout.setVisibility(View.VISIBLE);
                    if (otpBoxes == null || otpBoxes.length == 0) {
                        createBoxes(otpLayout, BOX_COUNT, false);
                    }

                    passcodeLayout.setVisibility(View.GONE);
                    tvOtpInstruction.setVisibility(View.VISIBLE);
                    btnProceed.setText("Verify OTP");
                    otpSent = true;

                    subText.setText("Enter OTP");
                    subText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                } else {
                    Toast.makeText(this, "Enter 6-digit passcode", Toast.LENGTH_SHORT).show();
                }
            } else {
                String enteredOtp = getBoxValue(otpBoxes);
                if (enteredOtp.length() == BOX_COUNT) {
                    verifyFinalOtp(mobile, enteredOtp);
                } else {
                    Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendOtpAgain(String mobile) {
        try {
            JSONObject params = new JSONObject();
            params.put("mobile", mobile);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    otpUrl + "/send",
                    params,
                    response -> Toast.makeText(this, "OTP sent again!", Toast.LENGTH_SHORT).show(),
                    error -> {
                        Toast.makeText(this, "Error sending OTP", Toast.LENGTH_SHORT).show();
                        Log.e("OTP_SEND_ERROR", error.toString());
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyFinalOtp(String mobile, String otp) {
        try {
            JSONObject params = new JSONObject();
            params.put("mobile", mobile);
            params.put("otp", otp);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    otpUrl + "/verify",
                    params,
                    response -> {
                        Toast.makeText(this, "✅ OTP Verified!", Toast.LENGTH_SHORT).show();
                        passcode = getBoxValue(passcodeBoxes); // Ensure correct passcode is sent
                        savePasscodeToDB(mobile, passcode,name );
                    },
                    error -> {
                        Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                        Log.e("OTP_VERIFY_ERROR", error.toString());
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePasscodeToDB(String mobile, String passcode,String name) {
        try {
            JSONObject params = new JSONObject();
            params.put("mobile", mobile);
            params.put("passcode", passcode);
            params.put("name", name);
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    userUrl + "/register",
                    params,
                    response -> {
                        Toast.makeText(this, "🎉 Passcode saved!", Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getSharedPreferences("SurakshaPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("mobile", mobile)
                                .apply();
                        new AlertDialog.Builder(Passcode.this)
                                .setTitle("Let’s Train Your Digital Bodyguard!")
                                .setMessage("Your app is learning how you swipe, type, and move —\nso it can spot intruders even after login.\n\n1) Tracks: Swipe speed | Typing style | Phone motion\n2) Data stays only on your phone\n3) No messages, passwords, or personal files are touched\n\nJust use the app like normal — protection starts now!")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog, which) -> {
                        startActivity(new Intent(Passcode.this, Homescreen.class));
                        finish();
                        })
                                .show();
                    },
                    error -> {
                        String errorMsg = error.networkResponse != null && error.networkResponse.data != null
                                ? new String(error.networkResponse.data)
                                : "Unknown error";
                        Toast.makeText(this, "❌ Failed to save passcode", Toast.LENGTH_SHORT).show();
                        Log.e("SAVE_PASSCODE_ERROR", errorMsg);
                    }
            );

            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createBoxes(LinearLayout layout, int count, boolean isPasscode) {
        // Prevent duplicate creation
        if (isPasscode && passcodeBoxes != null && passcodeBoxes.length == count) return;
        if (!isPasscode && otpBoxes != null && otpBoxes.length == count) return;

        layout.removeAllViews();
        EditText[] boxes = new EditText[count];

        for (int i = 0; i < count; i++) {
            EditText box = new EditText(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 150);
            params.setMargins(10, 0, 10, 0);
            box.setLayoutParams(params);
            box.setBackgroundResource(R.drawable.box_background);
            box.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            box.setTextSize(24);
            box.setInputType(InputType.TYPE_CLASS_NUMBER);
            box.setEms(1);
            box.setId(View.generateViewId());

            int finalI = i;
            box.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && finalI < count - 1) {
                        if (isPasscode) passcodeBoxes[finalI + 1].requestFocus();
                        else otpBoxes[finalI + 1].requestFocus();
                    }
                }
            });

            layout.addView(box);
            boxes[i] = box;
        }

        if (isPasscode) passcodeBoxes = boxes;
        else otpBoxes = boxes;

        boxes[0].requestFocus();
    }

    private String getBoxValue(EditText[] boxes) {
        StringBuilder sb = new StringBuilder();
        for (EditText box : boxes) {
            sb.append(box.getText().toString().trim());
        }
        return sb.toString();
    }
}
