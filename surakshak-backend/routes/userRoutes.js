const express = require('express');
const router = express.Router();
const User = require('../models/User');
const ResetLog = require('../models/ResetLog');
const bcrypt = require('bcrypt');

const SALT_ROUNDS = 10;

// ✅ Register or update passcode
router.post('/register', async (req, res) => {
    const { mobile, name, passcode } = req.body;

    if (!mobile || !passcode) {
        return res.status(400).json({ message: 'Mobile and passcode are required' });
    }

    try {
        const hashedPasscode = await bcrypt.hash(passcode, SALT_ROUNDS);
        let existingUser = await User.findOne({ mobile });

        if (existingUser) {
            existingUser.passcode = hashedPasscode;
            if (name) existingUser.name = name;
            await existingUser.save();
            return res.json({ message: 'Passcode updated successfully' });
        }

        const newUser = new User({ mobile, name, passcode: hashedPasscode });
        await newUser.save();

        res.status(201).json({ message: 'User registered successfully' });
    } catch (err) {
        console.error('Register Error:', err);
        res.status(500).json({ message: 'Internal server error' });
    }
});
// ✅ Login: verify hashed passcode using either userID or mobile
router.post('/login', async (req, res) => {
    const { userID, mobile, passcode } = req.body;

    if ((!userID && !mobile) || !passcode) {
        return res.status(400).json({ message: 'UserID or mobile and passcode are required' });
    }

    try {
        const user = userID
            ? await User.findById(userID)
            : await User.findOne({ mobile });

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        const match = await bcrypt.compare(passcode, user.passcode);
        if (!match) {
            return res.status(401).json({ message: 'Invalid passcode' });
        }

        res.json({ message: 'Login successful', userId: user._id });
    } catch (err) {
        console.error('Login Error:', err);
        res.status(500).json({ message: 'Internal server error' });
    }
});


// ✅ Get user name
router.post('/getName', async (req, res) => {
    const { mobile } = req.body;

    if (!mobile) {
        return res.status(400).json({ message: 'Mobile number is required' });
    }

    try {
        const user = await User.findOne({ mobile });
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.json({ name: user.name || "User" });
    } catch (err) {
        console.error('GetName Error:', err);
        res.status(500).json({ message: 'Server error' });
    }
});

// ✅ Reset check route: max 3 attempts per day
router.post('/reset-check', async (req, res) => {
    const { mobile } = req.body;

    if (!mobile) {
        return res.status(400).json({ message: 'Mobile number is required' });
    }

    try {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        const attemptsToday = await ResetLog.countDocuments({
            mobile,
            createdAt: { $gte: today }
        });

        if (attemptsToday >= 3) {
            return res.status(429).json({ message: 'Max 3 reset attempts allowed per day' });
        }

        await ResetLog.create({ mobile });

        return res.status(200).json({ message: 'Reset allowed' });
    } catch (err) {
        console.error('Reset-Check Error:', err);
        return res.status(500).json({ message: 'Server error' });
    }
});

// ✅ Set or Update TPIN
router.post('/set_tpin', async (req, res) => {
    const { mobile, tpin } = req.body;

    if (!mobile || !tpin) {
        return res.status(400).json({ message: 'Mobile number and TPIN are required' });
    }

    try {
        const user = await User.findOne({ mobile });
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        const hashedTPIN = await bcrypt.hash(tpin, SALT_ROUNDS);
        user.tpin = hashedTPIN;
        await user.save();

        return res.json({ message: 'TPIN set successfully' });
    } catch (err) {
        console.error('Set TPIN Error:', err);
        res.status(500).json({ message: 'Server error while setting TPIN' });
    }
});

// ✅ Verify TPIN
router.post('/verify-tpin', async (req, res) => {
    const { mobile, tpin } = req.body;

    if (!mobile || !tpin) {
        return res.status(400).json({ message: 'Mobile and TPIN required' });
    }

    try {
        const user = await User.findOne({ mobile });
        if (!user || !user.tpin) return res.status(404).json({ message: 'TPIN not set' });

        const match = await bcrypt.compare(tpin, user.tpin);
        if (!match) return res.status(401).json({ message: 'Invalid TPIN' });

        return res.status(200).json({ message: 'TPIN Verified' });
    } catch (err) {
        console.error('TPIN Verify Error:', err);
        res.status(500).json({ message: 'Server error' });
    }
});

module.exports = router;
