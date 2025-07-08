package com.akanksha.cyber;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

//need to be integrated with the homescreen
public class DashboardActivity extends AppCompatActivity implements SensorEventListener {

    private TextView statusText;
    private long touchStartTime;
    private long keyPressStartTime;
    private long lastKeyReleaseTime = 0;
    private boolean isMonitoring = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] lastAccel = new float[3];
    private double lastLoggedTilt = 0;
    private final double TILT_THRESHOLD = 0.2;

    private List<Long> dwellTimes = new ArrayList<>();
    private List<Long> flightTimes = new ArrayList<>();
    private List<Float> tapPressures = new ArrayList<>();
    private List<Float> tiltAngles = new ArrayList<>();
    private int backspaceCount = 0;
    private int swipeDirectionCode = 0;
    private double typingSpeed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button startButton = findViewById(R.id.startMonitoringBtn);
        Button stopButton = findViewById(R.id.stopMonitoringBtn);
        statusText = findViewById(R.id.statusText);
        EditText typingField = findViewById(R.id.typingField);
        ScrollView scrollView = findViewById(R.id.scrollView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        startButton.setOnClickListener(v -> {
            isMonitoring = true;
            statusText.setText("Monitoring Started...");
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        });

        stopButton.setOnClickListener(v -> {
            isMonitoring = false;
            statusText.setText("Monitoring Stopped.");
            sensorManager.unregisterListener(this);
        });

        typingField.setOnKeyListener((v, keyCode, event) -> {
            if (!isMonitoring) return false;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    keyPressStartTime = System.currentTimeMillis();
                    return false;
                case MotionEvent.ACTION_UP:
                    long keyReleaseTime = System.currentTimeMillis();
                    long dwellTime = keyReleaseTime - keyPressStartTime;
                    dwellTimes.add(dwellTime);

                    if (lastKeyReleaseTime != 0) {
                        long flightTime = keyPressStartTime - lastKeyReleaseTime;
                        flightTimes.add(flightTime);
                        Log.d("TypingData", "FlightTime: " + flightTime + "ms");
                    }

                    lastKeyReleaseTime = keyReleaseTime;
                    Log.d("TypingData", "DwellTime: " + dwellTime + "ms");
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
                    Log.d("TypingData", "Backspace used. Total: " + backspaceCount);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isMonitoring) return;

                long elapsed = System.currentTimeMillis() - startTime;
                int charCount = s.length();
                if (elapsed > 0 && charCount > 0) {
                    typingSpeed = (charCount * 60000.0) / elapsed;
                    Log.d("TypingData", "Typing Speed: " + typingSpeed + " chars/min");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!isMonitoring) return;
            int scrollY = scrollView.getScrollY();
            Log.d("ScrollData", "Scroll Y: " + scrollY);
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isMonitoring) {
                    buildAndLogBehaviorVector();
                }
                new Handler().postDelayed(this, 30000);
            }
        }, 30000);
    }

    private void buildAndLogBehaviorVector() {
        double avgDwell = averageList(dwellTimes);
        double avgFlight = averageList(flightTimes);
        double avgPressure = averageList(tapPressures);
        double avgTilt = averageList(tiltAngles);

        BehaviorVector vector = new BehaviorVector(
                typingSpeed,
                avgDwell,
                avgFlight,
                backspaceCount,
                avgPressure,
                swipeDirectionCode,
                avgTilt
        );

        Log.d("BehaviorVector", "Generated: " + vector.toString());

        dwellTimes.clear();
        flightTimes.clear();
        tapPressures.clear();
        tiltAngles.clear();
        backspaceCount = 0;
    }

    private double averageList(List<? extends Number> list) {
        if (list.isEmpty()) return 0;
        double sum = 0;
        for (Number num : list) sum += num.doubleValue();
        return sum / list.size();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!isMonitoring) return super.dispatchTouchEvent(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchStartTime = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_UP:
                long touchEndTime = System.currentTimeMillis();
                long duration = touchEndTime - touchStartTime;
                float x = event.getX();
                float y = event.getY();
                float pressure = event.getPressure();
                int pointerCount = event.getPointerCount();

                tapPressures.add(pressure);
                Log.d("TouchData", "Tap at (" + x + "," + y + "), Duration: " + duration +
                        "ms, Pressure: " + pressure + ", Fingers: " + pointerCount);
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

                Log.d("SwipeData", "Swipe Direction: " + swipeDirection);
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isMonitoring || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        double tilt = Math.sqrt(x * x + y * y + z * z);
        tiltAngles.add((float) tilt);

        if (Math.abs(tilt - lastLoggedTilt) > TILT_THRESHOLD) {
            Log.d("SensorData", "Tilt angle approximation: " + tilt);
            lastLoggedTilt = tilt;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
