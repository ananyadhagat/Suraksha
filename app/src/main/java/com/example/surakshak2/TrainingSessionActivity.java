package com.example.surakshak2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class TrainingSessionActivity extends Activity {

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
    Button nextBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_session);

        sentenceView = findViewById(R.id.sentence_to_type);
        inputField = findViewById(R.id.user_input);
        nextBtn = findViewById(R.id.next_button);
        stepView = findViewById(R.id.session_progress);

        updateScreen();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentStep >= trainingSentences.length) return;  // 🔒 prevent crash

                String typed = inputField.getText().toString().trim();
                String expected = trainingSentences[currentStep];

                if (!typed.equals(expected)) {
                    Toast.makeText(TrainingSessionActivity.this, "Typed sentence doesn't match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject session = new JSONObject();
                try {
                    session.put("sentence", expected);
                    session.put("typed_text", typed);
                    session.put("avg_typing_speed", 145); // Placeholder, replace with actual
                    session.put("swipe_length", 310); // Placeholder
                    session.put("gyro_pattern", 1); // Placeholder
                    session.put("accel_variance", 0.3); // Placeholder
                    trainingDataList.add(session);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                currentStep++;

                if (currentStep < trainingSentences.length) {
                    updateScreen();
                } else {
                    sendTrainingDataToBackend();
                }
            }
        });
    }

        void updateScreen() {
        sentenceView.setText(trainingSentences[currentStep]);
        inputField.setText("");
        stepView.setText("Step " + (currentStep + 1) + " of " + trainingSentences.length);
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

        String url = "http://192.168.29.36:5000/upload-training-batch"; // 🔁 IP

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, finalObject,
                response -> {
                    Toast.makeText(this, "✅ Training data sent!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(this, "❌ Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
