from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import datetime

#MACHINE LEARNING LIBRARIES
import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import MinMaxScaler
import xgboost as xgb
from keras.models import Sequential
from keras.layers import LSTM, Dense

#Define Preprocessing + Model Training Functions
# ✅ Preprocessing Function
def preprocess_training_data():
    documents = list(training_collection.find())
    df = pd.DataFrame(documents)

    # Drop MongoDB _id and timestamp
    df = df.drop(columns=['_id', 'timestamp'], errors='ignore')

    # Flatten batch if needed
    batch = pd.json_normalize(df['training_batch'].explode())
    batch.fillna(0, inplace=True)

    # Features and target
    X = batch.drop(columns=['typed_text'])  # or your actual target
    y = batch['typed_text']  # update this if needed (for classification use appropriate label)

    # Feature scaling
    scaler = MinMaxScaler()
    X_scaled = scaler.fit_transform(X)

    # Reshape for LSTM [samples, timesteps, features]
    X_lstm = X_scaled.reshape(X_scaled.shape[0], 1, X_scaled.shape[1])

    return X_lstm, X_scaled, y

# ✅ LSTM Model
def train_lstm(X_lstm, y):
    model = Sequential()
    model.add(LSTM(64, input_shape=(X_lstm.shape[1], X_lstm.shape[2])))
    model.add(Dense(32, activation='relu'))
    model.add(Dense(1, activation='linear'))  # change to softmax or sigmoid if classification
    model.compile(optimizer='adam', loss='mse')  # change loss if classification
    model.fit(X_lstm, y, epochs=5, batch_size=16, verbose=1)
    return model

# ✅ XGBoost Model
def train_xgboost(X, y):
    model = xgb.XGBRegressor(n_estimators=100, max_depth=5)
    model.fit(X, y)
    return model



app = Flask(__name__)
CORS(app, supports_credentials=True)

# ✅ MongoDB Connection 
client = MongoClient("mongodb+srv://ananya2004d:1anan.ya9@surakshak.aait0ep.mongodb.net/?retryWrites=true&w=majority&appName=Surakshak")
db = client['surakshak_db']
training_collection = db['training_sessions']

@app.route('/upload-training-batch', methods=['POST'])
def upload_training_batch():
    try:
        data = request.json
        print("\n📩 Received data:", data)

        # Add timestamp for tracking
        data['timestamp'] = datetime.datetime.now()

        # Insert into MongoDB
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

#train-hybrid-model
@app.route('/train-hybrid-model', methods=['POST'])
def train_hybrid_model():
    try:
        X_lstm, X_xgb, y = preprocess_training_data()

        # Train models
        lstm_model = train_lstm(X_lstm, y)
        xgb_model = train_xgboost(X_xgb, y)

        # Optional: Save models to disk
        # lstm_model.save("lstm_model.h5")
        # xgb_model.save_model("xgb_model.json")

        return jsonify({
            "status": "success",
            "message": "LSTM and XGBoost models trained successfully 🎯"
        }), 200

    except Exception as e:
        print("❌ Error during training:", str(e))
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500



if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
