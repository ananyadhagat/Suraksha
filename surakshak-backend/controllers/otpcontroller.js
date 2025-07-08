const Otp = require('../models/Otp');

// Generate random 6-digit OTP
const generateOtp = () => {
    return Math.floor(100000 + Math.random() * 900000).toString();
};

// POST /api/otp/send
// POST /api/otp/send
const sendOtp = async (req, res) => {
    const { mobile } = req.body;

    if (!mobile || mobile.length !== 10) {
        return res.status(400).json({ message: 'Invalid mobile number' });
    }

    const otp = generateOtp();

    try {
        // Remove previous OTPs for this number
        await Otp.deleteMany({ mobile });

        // Save new OTP
       const newOtp = new Otp({ mobile, otp });
       await newOtp.save().then(doc => {
       console.log("📦 OTP saved to MongoDB:", doc);
       }).catch(err => {
       console.error("❌ Failed to save OTP:", err);
       });


        console.log(`✅ OTP for ${mobile}: ${otp}`);  // Log OTP to console for judges

        // Send OTP in the response for frontend to display
        res.json({
            message: 'OTP sent successfully',
            otp: otp  // Send the OTP here
        });
    } catch (error) {
        console.error('Error sending OTP:', error);
        res.status(500).json({ message: 'Server error while sending OTP' });
    }
};


// POST /api/otp/verify
const verifyOtp = async (req, res) => {
    const { mobile, otp } = req.body || {};  // Safely handle undefined req.body

    if (!mobile || !otp) {
        return res.status(400).json({ message: 'Both mobile number and OTP are required' });
    }

    try {
        const record = await Otp.findOne({ mobile, otp });

        if (!record) {
            return res.status(400).json({ message: 'Invalid or expired OTP' });
        }

        // OTP matched, delete it
        await Otp.deleteMany({ mobile });

        res.json({ message: 'OTP verified successfully' });
    } catch (error) {
        console.error('Error verifying OTP:', error);
        res.status(500).json({ message: 'Server error while verifying OTP' });
    }
};

module.exports = { sendOtp, verifyOtp };
