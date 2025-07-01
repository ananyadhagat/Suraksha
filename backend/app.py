from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import datetime

app = Flask(__name__)
CORS(app, supports_credentials=True)

# ✅ MongoDB Setup
try:
    client = MongoClient("mongodb+srv://ananya2004d:1anan.ya9@surakshak.aait0ep.mongodb.net/?retryWrites=true&w=majority&appName=Surakshak")
    db = client["surakshak_db"]
    collection = db["training_sessions"]
    print("✅ MongoDB connected!")
except Exception as mongo_err:
    print("❌ MongoDB connection error:", mongo_err)

@app.route("/api/train-behavior", methods=["POST"])
def receive_behavior_data():
    try:
        # DEBUG: Raw request info
        print("📩 Request Headers:", request.headers)
        print("📩 Raw Body:", request.data)

        data = request.get_json(force=True)
        print("✅ Parsed JSON:", data)

        # Validation
        required_fields = [
            "typingSpeed", "dwellTime", "flightTime",
            "backspaceCount", "tapPressure", "swipeDirection",
            "tiltAngle", "screenHoldTime"
        ]
        for field in required_fields:
            if field not in data:
                print(f"⚠️ Missing field: {field}")
                return jsonify({"error": f"Missing field: {field}"}), 400

        data["timestamp"] = datetime.datetime.utcnow()

        # ✅ Insert to MongoDB
        result = collection.insert_one(data)
        print("✅ Inserted ID:", result.inserted_id)

        return jsonify({"message": "Behavior data saved successfully!"}), 200

    except Exception as e:
        print("❌ Exception:", str(e))
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
