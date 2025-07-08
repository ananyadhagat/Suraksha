const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
require('dotenv').config();

const otpRoutes = require('./routes/otp');
const userRoutes = require('./routes/userRoutes');

const app = express();

app.use(cors({
    origin: '*',
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));
app.use(express.json());

const mongoURI = process.env.MONGO_URI || 'mongodb://localhost:27017/surakshaApp';
mongoose.connect(mongoURI, {
    useNewUrlParser: true,
    useUnifiedTopology: true
})
.then(() => console.log('✅ Connected to MongoDB'))
.catch(err => console.error('❌ MongoDB Connection Error:', err));

app.use('/api/otp', otpRoutes);
app.use('/api/user', userRoutes);

const PORT = process.env.PORT || 5001;
app.listen(PORT, () => {
    console.log(`✅ Server running on port ${PORT}`);
});
