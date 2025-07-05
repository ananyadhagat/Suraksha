// models/ResetLog.js

const mongoose = require('mongoose');

const resetLogSchema = new mongoose.Schema({
  mobile: { type: String, required: true },
  createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('ResetLog', resetLogSchema);
