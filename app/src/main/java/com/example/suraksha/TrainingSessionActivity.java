package com.example.suraksha;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class TrainingSessionActivity extends Activity implements SensorEventListener {

    EditText etTypingArea;
    View gestureZone;
    Button btnSubmit;

    long sessionStartTime;
    long lastKeyTime;
    long keyPressTime;
    int totalChars = 0;
    int backspaceCount = 0;

    ArrayList<Long> dwellTimes = new ArrayList<>();
    ArrayList<Long> flightTimes = new ArrayList<>();

    float totalTapPressure = 0;
    int tapCount = 0;
    float pitch = 0f;
    String swipeDirection = "None";

    GestureDetector gestureDetector;
    SensorManager sensorManager;
    Sensor rotationSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        etTypingArea = findViewById(R.id.etTypingArea);
        gestureZone = findViewById(R.id.gestureZone);
        btnSubmit = findViewById(R.id.btnSubmitTraining);
        sessionStartTime = System.currentTimeMillis();

        // ✅ Initialize lists
        dwellTimes = new ArrayList<>();
        flightTimes = new ArrayList<>();

        etTypingArea.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                keyPressTime = System.currentTimeMillis();
                if (keyCode == 67) backspaceCount++;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                long releaseTime = System.currentTimeMillis();
                long dwell = releaseTime - keyPressTime;
                dwellTimes.add(dwell);

                if (lastKeyTime != 0) {
                    long flight = keyPressTime - lastKeyTime;
                    flightTimes.add(flight);
                }

                lastKeyTime = releaseTime;
                totalChars++;
            }
            return false;
        });




    gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                swipeDirection = (Math.abs(diffX) > Math.abs(diffY)) ? (diffX > 0 ? "Right" : "Left") : (diffY > 0 ? "Down" : "Up");
                return true;
            }
        });

        gestureZone.setOnTouchListener((v, event) -> {
            tapCount++;
            totalTapPressure += event.getPressure();
            gestureDetector.onTouchEvent(event);
            return true;
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        btnSubmit.setOnClickListener(v -> finishTraining() );



    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);
        pitch = (float) Math.toDegrees(orientation[1]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void finishTraining() {

            long sessionTime = System.currentTimeMillis() - sessionStartTime;

            double avgDwell = dwellTimes.stream().mapToLong(i -> i).average().orElse(0);
            double avgFlight = flightTimes.stream().mapToLong(i -> i).average().orElse(0);
            double typingSpeed = sessionTime / (double) Math.max(totalChars, 1);
            double avgPressure = (tapCount > 0) ? totalTapPressure / tapCount : 0;

            try {
                JSONObject behavior = new JSONObject();
                behavior.put("typingSpeed", Double.isNaN(typingSpeed) ? 0 : typingSpeed);
                behavior.put("dwellTime", Double.isNaN(avgDwell) ? 0 : avgDwell);
                behavior.put("flightTime", Double.isNaN(avgFlight) ? 0 : avgFlight);
                behavior.put("backspaceCount", backspaceCount);
                behavior.put("tapPressure", Double.isNaN(avgPressure) ? 0 : avgPressure);
                behavior.put("swipeDirection", swipeDirection != null ? swipeDirection : "None");
                behavior.put("tiltAngle", Double.isNaN(pitch) ? 0 : pitch);
                behavior.put("screenHoldTime", sessionTime / 1000);

                // ✅ Show local confirmation
                Toast.makeText(this, "Captured! Sending to backend...", Toast.LENGTH_SHORT).show();
                Log.d("BEHAVIOR_JSON", behavior.toString());

                new Thread(() -> {
                    HttpURLConnection conn = null;
                    try {
                        URL url = new URL("http://192.168.29.36:5000/api/train-behavior"); // ✅ Your Flask IP
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; utf-8");
                        conn.setRequestProperty("Accept", "application/json");
                        conn.setDoOutput(true);

                        OutputStream os = conn.getOutputStream();
                        byte[] input = behavior.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                        os.flush();
                        os.close();

                        int responseCode = conn.getResponseCode();
                        Log.d("FLASK_RESPONSE_CODE", String.valueOf(responseCode));

                        if (responseCode == 200) {
                            runOnUiThread(() ->
                                    Toast.makeText(TrainingSessionActivity.this, "✅ Data sent to Flask!", Toast.LENGTH_SHORT).show()
                            );
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(TrainingSessionActivity.this, "❌ Server Error: " + responseCode, Toast.LENGTH_LONG).show()
                            );
                            Log.e("SERVER_ERROR", "Response code: " + responseCode);
                        }

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(TrainingSessionActivity.this, "❌ Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                        Log.e("NETWORK_ERROR", e.toString());

                    } finally {
                        if (conn != null) conn.disconnect();
                    }
                }).start();

            } catch (Exception e) {
                Toast.makeText(this, "❌ JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("JSON_ERROR", e.toString());
            }

            // ✅ Stop sensors
            sensorManager.unregisterListener(this);
            finish();
        }
    }

