package com.example.surakshak2;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrainingSessionActivity extends Activity implements SensorEventListener {

    String[] trainingSentences = {
            "The quick brown fox jumps over the lazy dog.",
            "Security is more about behavior than passwords.",
            "My typing speed is unique like a fingerprint.",
            "I bank safely from my mobile every day."
    };

    int currentStep = 0;
    List<JSONObject> trainingDataList = new ArrayList<>();

    TextView sentenceView, stepView;
    EditText inputField;
    Button nextBtn, trainModelButton;

    long lastKeyDownTime = 0;
    long lastKeyUpTime = 0;
    long lastTouchTime = 0;

    ArrayList<Long> holdDurations = new ArrayList<>();
    ArrayList<Long> flightTimes = new ArrayList<>();
    ArrayList<Float> pressures = new ArrayList<>();
    ArrayList<Float> sizes = new ArrayList<>();
    ArrayList<Long> interKeyDelays = new ArrayList<>();

    long typingStartTime = 0;
    long typingEndTime = 0;
    long screenHoldStartTime = 0;
    long screenHoldEndTime = 0;

    SensorManager sensorManager;
    Sensor accelSensor, gyroSensor;
    ArrayList<Float> accelValues = new ArrayList<>();
    ArrayList<Float> gyroValues = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        sentenceView = findViewById(R.id.sentence_to_type);
        inputField = findViewById(R.id.user_input);
        nextBtn = findViewById(R.id.next_button);
        stepView = findViewById(R.id.session_progress);
        trainModelButton = findViewById(R.id.train_model_button);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        updateScreen();

        inputField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                long now = System.currentTimeMillis();
                pressures.add(event.getPressure());
                sizes.add(event.getSize());
                if (lastTouchTime != 0) {
                    interKeyDelays.add(now - lastTouchTime);
                }
                lastTouchTime = now;
            }
            return false;
        });

        inputField.setOnKeyListener((v, keyCode, event) -> {
            long now = System.currentTimeMillis();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastKeyDownTime = now;
                    if (lastKeyUpTime != 0) {
                        flightTimes.add(now - lastKeyUpTime);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (lastKeyDownTime != 0) {
                        holdDurations.add(now - lastKeyDownTime);
                    }
                    lastKeyUpTime = now;
                    break;
            }
            return false;
        });

        nextBtn.setOnClickListener(v -> {
            if (currentStep >= trainingSentences.length) return;

            String typed = inputField.getText().toString().trim();
            String expected = trainingSentences[currentStep];

            if (!typed.equals(expected)) {
                Toast.makeText(this, "Typed sentence doesn't match!", Toast.LENGTH_SHORT).show();
                return;
            }

            typingEndTime = System.currentTimeMillis();
            screenHoldEndTime = System.currentTimeMillis();

            long typingDurationMillis = typingEndTime - typingStartTime;
            double typingSpeed = (typed.length() / (typingDurationMillis / 1000.0));
            long screenHoldTime = screenHoldEndTime - screenHoldStartTime;

            JSONObject session = new JSONObject();
            try {
                session.put("sentence", expected);
                session.put("typed_text", typed);
                session.put("hold_times", new JSONArray(holdDurations));
                session.put("flight_times", new JSONArray(flightTimes));
                session.put("pressures", new JSONArray(pressures));
                session.put("touch_sizes", new JSONArray(sizes));
                session.put("inter_key_delays", new JSONArray(interKeyDelays));
                session.put("accel_pattern", new JSONArray(accelValues));
                session.put("gyro_pattern", new JSONArray(gyroValues));
                session.put("avg_typing_speed", typingSpeed);
                session.put("screen_hold_time", screenHoldTime);

                trainingDataList.add(session);
                generateBehaviorVector();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            currentStep++;
            resetBehavioralArrays();

            if (currentStep < trainingSentences.length) {
                updateScreen();
            } else {
                sendTrainingDataToBackend();
            }
        });

        trainModelButton.setOnClickListener(v -> triggerMLModelTraining());
    }

    void generateBehaviorVector() {
        double avgHold = average(holdDurations);
        double avgFlight = average(flightTimes);
        double avgInterDelay = average(interKeyDelays);
        double avgPressure = averageFloat(pressures);
        double avgSize = averageFloat(sizes);
        double avgTilt = averageFloat(accelValues);
        double typingSpeed = calculateTypingSpeed();
        long screenHoldTime = screenHoldEndTime - screenHoldStartTime;

        String behaviorVector = String.format(
                "Vector[TypingSpeed=%.2f, DwellTime=%.2f, FlightTime=%.2f, InterKeyDelay=%.2f, TapPressure=%.2f, TouchSize=%.2f, TiltAngle=%.2f, ScreenHoldTime=%d]",
                typingSpeed, avgHold, avgFlight, avgInterDelay, avgPressure, avgSize, avgTilt, screenHoldTime
        );

        android.util.Log.d("BehaviorVector", "Generated: " + behaviorVector);
    }

    double average(ArrayList<Long> list) {
        if (list.isEmpty()) return 0;
        long sum = 0;
        for (long val : list) sum += val;
        return (double) sum / list.size();
    }

    double averageFloat(ArrayList<Float> list) {
        if (list.isEmpty()) return 0;
        float sum = 0;
        for (float val : list) sum += val;
        return sum / list.size();
    }

    double calculateTypingSpeed() {
        long durationMillis = typingEndTime - typingStartTime;
        if (durationMillis == 0) return 0;
        String typed = inputField.getText().toString().trim();
        return typed.length() / (durationMillis / 1000.0);
    }

    void updateScreen() {
        sentenceView.setText(trainingSentences[currentStep]);
        inputField.setText("");
        stepView.setText("Step " + (currentStep + 1) + " of " + trainingSentences.length);
        typingStartTime = System.currentTimeMillis();
        screenHoldStartTime = System.currentTimeMillis();
    }

    void resetBehavioralArrays() {
        holdDurations.clear();
        flightTimes.clear();
        pressures.clear();
        sizes.clear();
        interKeyDelays.clear();
        accelValues.clear();
        gyroValues.clear();
        lastKeyDownTime = 0;
        lastKeyUpTime = 0;
        lastTouchTime = 0;
        typingStartTime = 0;
        typingEndTime = 0;
        screenHoldStartTime = 0;
        screenHoldEndTime = 0;
    }

    void sendTrainingDataToBackend() {
        JSONObject finalObject = new JSONObject();
        JSONArray batch = new JSONArray(trainingDataList);
        try {
            finalObject.put("userID", "user_01");
            finalObject.put("training_batch", batch);
            finalObject.put("label", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://192.168.29.36:5000/upload-training-batch";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, finalObject,
                response -> Toast.makeText(this, "✅ Training data sent!", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "❌ Error: " + error.toString(), Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    void triggerMLModelTraining() {
        String url = "http://192.168.29.36:5000/train-hybrid-model";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> Toast.makeText(this, "✅ ML Training Done!", Toast.LENGTH_LONG).show(),
                error -> Toast.makeText(this, "❌ ML Error: " + error.toString(), Toast.LENGTH_LONG).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float totalAccel = (float) Math.sqrt(
                    event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]);
            accelValues.add(totalAccel);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float totalGyro = (float) Math.sqrt(
                    event.values[0] * event.values[0] +
                            event.values[1] * event.values[1] +
                            event.values[2] * event.values[2]);
            gyroValues.add(totalGyro);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}