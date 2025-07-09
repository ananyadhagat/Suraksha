package com.example.suraksha;

import android.content.Context;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.JSONObject;

public class RiskAnalyzer {
    private static final String TAG = "RiskAnalyzer";
    private static final String backendUrl = "http://172.16.19.12:5000/evaluate-risk";

    public static void evaluateRisk(Context context, JSONObject behaviorVector) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                backendUrl,
                behaviorVector,
                response -> {
                    double riskScore = response.optDouble("riskScore", -1);
                    Log.d(TAG, "🧠 Risk Score: " + riskScore);
                },
                error -> {
                    Log.e(TAG, "Risk evaluation failed: " + error.toString());
                }
        );
        Volley.newRequestQueue(context).add(request);
    }
}
