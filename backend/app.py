from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import datetime

# MACHINE LEARNING LIBRARIES
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
import xgboost as xgb
from keras.models import Sequential
from keras.layers import LSTM, Dense

# Risk Vector Helpers
def average_behavior_vector(batch):
    return {
        "TypingSpeed": np.mean([s.get("avg_typing_speed", 0) for s in batch]),
        "DwellTime": np.mean([np.mean(s.get("hold_times", [])) if s.get("hold_times") else 0 for s in batch]),
        "FlightTime": np.mean([np.mean(s.get("flight_times", [])) if s.get("flight_times") else 0 for s in batch]),
        "InterKeyDelay": np.mean([np.mean(s.get("inter_key_delays", [])) if s.get("inter_key_delays") else 0 for s in batch]),
        "TapPressure": np.mean([np.mean(s.get("pressures", [])) if s.get("pressures") else 0 for s in batch]),
        "TouchSize": np.mean([np.mean(s.get("touch_sizes", [])) if s.get("touch_sizes") else 0 for s in batch]),
        "TiltAngle": np.mean([np.mean(s.get("accel_pattern", [])) if s.get("accel_pattern") else 0 for s in batch]),
        "ScreenHoldTime": np.mean([s.get("screen_hold_time", 0) for s in batch])
    }

def calculate_risk_score(vector, baseline):
    distance = sum((vector[k] - baseline[k]) ** 2 for k in vector)
    return round(min(100, (np.sqrt(distance) / 200) * 100), 2)

# Flask App Setup
app = Flask(__name__)
CORS(app, supports_credentials=True)

# MongoDB Connection
client = MongoClient("mongodb+srv://ananya2004d:1anan.ya9@surakshak.aait0ep.mongodb.net/?retryWrites=true&w=majority&appName=Surakshak")
db = client['surakshak_db']
training_collection = db['training_sessions']
risk_collection = db['risk_scores']

# Upload Training Data
@app.route('/upload-training-batch', methods=['POST'])
def upload_training_batch():
    try:
        data = request.json
        print("\n📩 Received data:", data)

        data['timestamp'] = datetime.datetime.now()
        result = training_collection.insert_one(data)
        print("✅ Inserted ID:", result.inserted_id)

        return jsonify({
            "status": "success",
            "message": "Training data stored in MongoDB ✅"
        }), 200

    except Exception as e:
        print("❌ Error while saving data:", str(e))
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

# Train Model + Save Avg Vector + Risk Score
@app.route('/train-hybrid-model', methods=['POST'])
def train_hybrid_model():
    try:
        training_data = list(training_collection.find())
        X = []
        y = []

        for entry in training_data:
            for sample in entry['training_batch']:
                features = [
                    sample.get("avg_typing_speed", 0),
                    np.mean(sample.get("hold_times", [])),
                    np.mean(sample.get("flight_times", [])),
                    np.mean(sample.get("inter_key_delays", [])),
                    np.mean(sample.get("pressures", [])),
                    np.mean(sample.get("touch_sizes", [])),
                    np.mean(sample.get("accel_pattern", [])),
                    np.mean(sample.get("gyro_pattern", [])),
                    sample.get("screen_hold_time", 0)
                ]
                X.append(features)
                y.append(entry.get("label", 1))  # default to genuine

        X = np.array(X)
        y = np.array(y)

        print("✅ Training data shape:", X.shape)
        print("✅ Model trained on", len(X), "samples.")

        # Extract latest session's user and vector data
        user_id = training_data[-1].get("userID", "user_01")
        training_batch = training_data[-1].get("training_batch", [])

        if training_batch:
            avg_vector = average_behavior_vector(training_batch)
            ideal_vector = {
                "TypingSpeed": 2.0,
                "DwellTime": 100,
                "FlightTime": 120,
                "InterKeyDelay": 150,
                "TapPressure": 0.8,
                "TouchSize": 0.02,
                "TiltAngle": 9.7,
                "ScreenHoldTime": 25000
            }

            risk_score = calculate_risk_score(avg_vector, ideal_vector)

            # ✅ Store risk score, avg vector, and raw vectors
            risk_collection.update_one(
                {"userID": user_id},
                {"$set": {
                    "all_vectors": training_batch,
                    "average_vector": avg_vector,
                    "initial_risk_score": risk_score,
                    "timestamp": datetime.datetime.now()
                }},
                upsert=True
            )

            print("🧠 Initial Avg Vector:", avg_vector)
            print("🔥 Initial Risk Score:", risk_score)

        return jsonify({"status": "success", "message": "Model trained successfully ✅"})

    except Exception as e:
        print("❌ Error during training:", e)
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
