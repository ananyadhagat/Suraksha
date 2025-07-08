package com.example.suraksha;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Homescreen extends AppCompatActivity implements SensorEventListener {

    GridLayout payGrid, upiGrid, privacyGrid;

    private long touchStartTime;
    private long keyPressStartTime;
    private long lastKeyReleaseTime = 0;
    private boolean isMonitoring = true;

    private SensorManager sensorManager;
    private Sensor accelerometer, gyroscope;

    private List<Long> dwellTimes = new ArrayList<>();
    private List<Long> flightTimes = new ArrayList<>();
    private List<Float> tapPressures = new ArrayList<>();
    private List<Float> tiltAngles = new ArrayList<>();
    private List<Float> gyroValues = new ArrayList<>();
    private List<Long> interKeyDelays = new ArrayList<>();
    private List<Float> touchSizes = new ArrayList<>();

    private int backspaceCount = 0;
    private int swipeDirectionCode = 0;
    private double typingSpeed = 0;

    private long lastTouchTime = 0;
    private long typingStartTime = 0;
    private long typingEndTime = 0;
    private long screenHoldStartTime = 0;
    private long screenHoldEndTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);

        payGrid = findViewById(R.id.payGrid);
        upiGrid = findViewById(R.id.upiGrid);
        privacyGrid = findViewById(R.id.privacyGrid);

        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        logoutIcon.setOnClickListener(v -> showLogoutDialog());

        addIconToGrid(payGrid, R.drawable.ic_send, "Send Money");
        addIconToGrid(payGrid, R.drawable.ic_bill, "Bill Payment");
        addIconToGrid(payGrid, R.drawable.ic_card, "Card-less Pay");
        addIconToGrid(payGrid, R.drawable.ic_beneficiary, "My Beneficiary");
        addIconToGrid(payGrid, R.drawable.ic_passbook, "ePassbook");
        addIconToGrid(payGrid, R.drawable.ic_balance, "Account Balance");

        addIconToGrid(upiGrid, R.drawable.upi, "Send to UPI ID");
        addIconToGrid(upiGrid, R.drawable.ic_qr, "Scan QR");
        addIconToGrid(upiGrid, R.drawable.ic_contact, "Pay to Mobile Number");
        addIconToGrid(upiGrid, R.drawable.ic_tap, "Tap & Pay");
        addIconToGrid(upiGrid, R.drawable.ic_receive, "Receive Money");
        addIconToGrid(upiGrid, R.drawable.ic_download, "Download UPI Statement");

        addIconToGrid(privacyGrid, R.drawable.ic_track, "Track Your Behaviour");
        addIconToGrid(privacyGrid, R.drawable.ic_optout, "Opt-out from Tracking");
        addIconToGrid(privacyGrid, R.drawable.ic_passcode, "Change Passcode");
        addIconToGrid(privacyGrid, R.drawable.ic_finger, "Enable Biometric Authentication");
        addIconToGrid(privacyGrid, R.drawable.ic_location, "Enable Location");
        addIconToGrid(privacyGrid, R.drawable.ic_help, "Help & Support");

        EditText typingField = findViewById(R.id.typingField);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);

        typingStartTime = System.currentTimeMillis();
        screenHoldStartTime = System.currentTimeMillis();

        typingField.setOnTouchListener((v, event) -> {
            if (!isMonitoring) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();
                tapPressures.add(event.getPressure());
                touchSizes.add(event.getSize());
                if (lastTouchTime != 0) {
                    interKeyDelays.add(now - lastTouchTime);
                }
                lastTouchTime = now;
            }
            return false;
        });

        typingField.setOnKeyListener((v, keyCode, event) -> {
            long now = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    keyPressStartTime = now;
                    if (lastKeyReleaseTime != 0) {
                        flightTimes.add(now - lastKeyReleaseTime);
                    }
                    return false;
                case MotionEvent.ACTION_UP:
                    long dwellTime = now - keyPressStartTime;
                    dwellTimes.add(dwellTime);
                    lastKeyReleaseTime = now;
                    return false;
            }
            return false;
        });

        typingField.addTextChangedListener(new TextWatcher() {
            private long startTime = System.currentTimeMillis();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isMonitoring) return;
                if (count > after) {
                    backspaceCount++;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isMonitoring) return;
                long elapsed = System.currentTimeMillis() - startTime;
                int charCount = s.length();
                if (elapsed > 0 && charCount > 0) {
                    typingSpeed = (charCount * 60000.0) / elapsed;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMonitoring) {
                    screenHoldEndTime = System.currentTimeMillis();
                    typingEndTime = System.currentTimeMillis();
                    buildAndSendBehaviorVector();
                }
                new Handler().postDelayed(this, 20000);
            }
        }, 20000);
    }

    private JSONObject buildAndSendBehaviorVector() {
        double avgDwell = averageList(dwellTimes);
        double avgFlight = averageList(flightTimes);
        double avgPressure = averageList(tapPressures);
        double avgTilt = averageList(tiltAngles);
        double avgGyro = averageList(gyroValues);
        double avgInterDelay = averageList(interKeyDelays);
        double avgTouchSize = averageList(touchSizes);
        long screenHoldTime = screenHoldEndTime - screenHoldStartTime;

        JSONObject vector = new JSONObject();
        try {
            vector.put("userID", "user_01");
            vector.put("TypingSpeed", typingSpeed);
            vector.put("DwellTime", avgDwell);
            vector.put("FlightTime", avgFlight);
            vector.put("InterKeyDelay", avgInterDelay);
            vector.put("TapPressure", avgPressure);
            vector.put("TouchSize", avgTouchSize);
            vector.put("TiltAngle", avgTilt);
            vector.put("ScreenHoldTime", screenHoldTime);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendVectorToServer(vector);

        dwellTimes.clear();
        flightTimes.clear();
        tapPressures.clear();
        tiltAngles.clear();
        gyroValues.clear();
        interKeyDelays.clear();
        touchSizes.clear();
        backspaceCount = 0;
        typingStartTime = System.currentTimeMillis();
        screenHoldStartTime = System.currentTimeMillis();

        return vector;
    }

    private void sendVectorToServer(JSONObject vector) {
        String url = "http://172.16.19.12:5000/evaluate-risk"; // Replace with your actual IP

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, vector,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                double riskScore = response.getDouble("risk_score");
                                Log.d("RiskScore", "Received: " + riskScore);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("RiskScore", "Error: " + error.toString());
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    private double averageList(List<? extends Number> list) {
        if (list.isEmpty()) return 0;
        double sum = 0;
        for (Number num : list) sum += num.doubleValue();
        return sum / list.size();
    }

    private void addIconToGrid(GridLayout grid, int iconRes, String label) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        itemLayout.setPadding(8, 16, 8, 16);

        ImageView iconView = new ImageView(this);
        iconView.setImageResource(iconRes);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(80, 80);
        iconView.setLayoutParams(iconParams);

        TextView textView = new TextView(this);
        textView.setText(label);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(14);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 8, 0, 0);
        textView.setMaxLines(2);

        itemLayout.addView(iconView);
        itemLayout.addView(textView);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setMargins(12, 12, 12, 12);
        params.width = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        itemLayout.setLayoutParams(params);

        grid.addView(itemLayout);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_logout_confirm, null);
        builder.setView(dialogView);

        Button btnYes = dialogView.findViewById(R.id.btnYes);
        Button btnNo = dialogView.findViewById(R.id.btnNo);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnNo.setOnClickListener(v -> dialog.dismiss());

        btnYes.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SurakshaPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();
            dialog.dismiss();
            finishAffinity();
        });

        dialog.show();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isMonitoring) return super.dispatchTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchStartTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                float pressure = event.getPressure();
                tapPressures.add(pressure);
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - event.getHistoricalX(0);
                float dy = event.getY() - event.getHistoricalY(0);
                String swipeDirection = Math.abs(dx) > Math.abs(dy)
                        ? (dx > 0 ? "Right" : "Left")
                        : (dy > 0 ? "Down" : "Up");

                switch (swipeDirection) {
                    case "Left": swipeDirectionCode = 1; break;
                    case "Right": swipeDirectionCode = 2; break;
                    case "Up": swipeDirectionCode = 3; break;
                    case "Down": swipeDirectionCode = 4; break;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isMonitoring) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float tilt = (float) Math.sqrt(x * x + y * y + z * z);
            tiltAngles.add(tilt);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float gx = event.values[0];
            float gy = event.values[1];
            float gz = event.values[2];
            float gyroMagnitude = (float) Math.sqrt(gx * gx + gy * gy + gz * gz);
            gyroValues.add(gyroMagnitude);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
