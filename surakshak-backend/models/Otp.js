const mongoose = require('mongoose');

const otpSchema = new mongoose.Schema({
    mobile: {
        type: String,
        required: true,
        minlength: 10,  // Ensures mobile is at least 10 digits long
        maxlength: 10,  // Ensures mobile is at most 10 digits long
    },
    otp: {
        type: String,
        required: true,
        minlength: 6,  // OTP is always 6 digits
        maxlength: 6,
    },
    createdAt: {
        type: Date,
        default: Date.now,
        expires: 300 // Expires after 5 minutes (TTL Index)
    }
});

module.exports = mongoose.model('Otp', otpSchema);
