const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    mobile: {
        type: String,
        required: true,
        unique: true
    },
    name: { type: String },
    passcode: {
        type: String,
        required: true
    },
    tpin: {
        type: String, // encrypted using SHA-256 from Android
        required: false
    }
});

module.exports = mongoose.model('User', userSchema);
