package com.akanksha.cyber;

import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.text.TextWatcher;
import android.text.Editable;
import android.util.Log;
import android.view.ViewTreeObserver;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView statusText;
    private long touchStartTime;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button startButton = findViewById(R.id.startMonitoringBtn);
        Button stopButton = findViewById(R.id.stopMonitoringBtn);
        statusText = findViewById(R.id.statusText);
        EditText typingField = findViewById(R.id.typingField);
        ScrollView scrollView = findViewById(R.id.scrollView);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMonitoring = true;
                statusText.setText("Monitoring Started...");
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMonitoring = false;
                statusText.setText("Monitoring Stopped.");
            }
        });

        // Typing behavior tracker
        typingField.addTextChangedListener(new TextWatcher() {
            private long lastKeyTime = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!isMonitoring) return;

                if (count > after) {
                    Log.d("TypingData", "Backspace detected at " + System.currentTimeMillis());
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isMonitoring) return;

                long currentTime = System.currentTimeMillis();
                if (lastKeyTime != 0) {
                    long interval = currentTime - lastKeyTime;
                    Log.d("TypingData", "Interval between keystrokes: " + interval + "ms");
                }
                lastKeyTime = currentTime;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Scroll behavior tracking
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!isMonitoring) return;

            int scrollY = scrollView.getScrollY();  // vertical scroll offset
            long time = System.currentTimeMillis();

            Log.d("ScrollData", "Scrolled to Y: " + scrollY + " at " + time);
        });
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
                int fingerCount = event.getPointerCount();

                statusText.setText("Touch: (" + x + ", " + y + ")\nDuration: " + duration + "ms");

                Log.d("TouchData", "x: " + x + ", y: " + y +
                        ", duration: " + duration + "ms, pressure: " + pressure +
                        ", fingers: " + fingerCount + ", time: " + touchEndTime);
                break;
        }

        return super.dispatchTouchEvent(event);
    }
}
