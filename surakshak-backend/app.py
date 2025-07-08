
from flask import Flask, request, jsonify
from flask_cors import CORS
from pymongo import MongoClient
import datetime

app = Flask(__name__)
CORS(app, supports_credentials=True)

#  MongoDB Connection 
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

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
