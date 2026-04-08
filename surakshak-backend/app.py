from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import datetime
import numpy as np
import xgboost as xgb
from keras.models import Sequential
from keras.layers import LSTM, Dense
from sklearn.preprocessing import MinMaxScaler

app = Flask(__name__)
CORS(app)

# MongoDB
client = MongoClient("your_mongo_uri")
db = client["surakshak_db"]
training_collection = db["training_sessions"]
risk_collection = db["risk_scores"]

# ---------- Helper ----------
def safe_avg(arr):
    return float(np.mean(arr)) if arr else 0.0

def extract_features(sample):
    return [
        sample.get("avg_typing_speed", 0),
        safe_avg(sample.get("hold_times", [])),
        safe_avg(sample.get("flight_times", [])),
        safe_avg(sample.get("inter_key_delays", [])),
        safe_avg(sample.get("pressures", [])),
        safe_avg(sample.get("touch_sizes", [])),
        safe_avg(sample.get("accel_pattern", [])),
        safe_avg(sample.get("gyro_pattern", [])),
        sample.get("screen_hold_time", 0)
    ]

def average_vector(batch):
    vectors = [extract_features(s) for s in batch]
    return np.mean(vectors, axis=0)

# ---------- ML TRAIN ----------
@app.route('/train-hybrid-model', methods=['POST'])
def train_model():
    try:
        data = list(training_collection.find())
        latest = data[-1]

        user_id = latest["userID"]
        batch = latest["training_batch"]

        # 🔹 Feature extraction
        X = np.array([extract_features(s) for s in batch])
        y = np.ones(len(X))  # normal user

        # 🔹 Scaling
        scaler = MinMaxScaler()
        X_scaled = scaler.fit_transform(X)

        # 🔹 LSTM
        X_lstm = X_scaled.reshape(X_scaled.shape[0], 1, X_scaled.shape[1])
        lstm = Sequential()
        lstm.add(LSTM(32, input_shape=(1, X.shape[1])))
        lstm.add(Dense(1))
        lstm.compile(optimizer='adam', loss='mse')
        lstm.fit(X_lstm, y, epochs=3, verbose=0)

        # 🔹 XGBoost
        xgb_model = xgb.XGBClassifier()
        xgb_model.fit(X_scaled, y)

        # 🔹 Baseline vector
        avg_vec = average_vector(batch)

        # Save baseline
        risk_collection.update_one(
            {"userID": user_id},
            {"$set": {
                "baseline_vector": avg_vec.tolist(),
                "timestamp": datetime.datetime.now()
            }},
            upsert=True
        )

        return jsonify({
            "status": "success",
            "message": "Model trained + baseline saved"
        })

    except Exception as e:
        return jsonify({"error": str(e)})

# ---------- RISK ----------
@app.route('/evaluate-risk', methods=['POST'])
def evaluate():
    try:
        data = request.json
        user_id = data["userID"]

        record = risk_collection.find_one({"userID": user_id})
        baseline = np.array(record["baseline_vector"])

        current = np.array([
            data["TypingSpeed"],
            data["DwellTime"],
            data["FlightTime"],
            data["InterKeyDelay"],
            data["TapPressure"],
            data["TouchSize"],
            data["TiltAngle"],
            data["GyroPattern"],
            data["ScreenHoldTime"]
        ])

        # Distance
        dist = np.linalg.norm(current - baseline)

        risk = min(100, (dist / 200) * 100)

        return jsonify({
            "risk_score": round(risk, 2)
        })

    except Exception as e:
        return jsonify({"error": str(e)})

if __name__ == '__main__':
    app.run(debug=True)
