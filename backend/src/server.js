require('dotenv').config();
// Force IPv4 DNS — prevents ENETUNREACH/EHOSTUNREACH on IPv6-only routes
const dns = require('dns');
dns.setDefaultResultOrder('ipv4first');

const express = require('express');
const nodemailer = require('nodemailer');

const app = express();
app.use(express.json());

const transporter = nodemailer.createTransport({
  host: 'smtp.gmail.com',
  port: 465,
  secure: true,   // SSL on 465
  family: 4,      // force IPv4 socket
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_PASS, // App Password, not your Gmail password
  },
});

// email -> { code: string, expiresAt: number }
const pendingCodes = new Map();
const CODE_TTL_MS = 10 * 60 * 1000; // 10 minutes

function generateCode() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

// POST /api/send-code   body: { email }
app.post('/api/send-code', async (req, res) => {
  const { email } = req.body;
  if (!email || typeof email !== 'string') {
    return res.status(400).json({ error: 'email is required' });
  }

  const code = generateCode();
  pendingCodes.set(email.toLowerCase(), { code, expiresAt: Date.now() + CODE_TTL_MS });

  try {
    await transporter.sendMail({
      from: `WordLy <${process.env.GMAIL_USER}>`,
      to: email,
      subject: 'WordLy — your verification code',
      text: `Your WordLy verification code: ${code}\n\nThis code expires in 10 minutes.`,
      html: `
        <div style="font-family:sans-serif;max-width:400px;margin:auto">
          <h2 style="color:#3B5998">WordLy</h2>
          <p>Your email verification code:</p>
          <h1 style="letter-spacing:8px;color:#222">${code}</h1>
          <p style="color:#888;font-size:13px">Expires in 10 minutes. If you didn't request this, ignore this email.</p>
        </div>
      `,
    });
    return res.json({ success: true });
  } catch (err) {
    console.error('Mail error:', err.message);
    pendingCodes.delete(email.toLowerCase());
    return res.status(500).json({ error: 'Failed to send email: ' + err.message });
  }
});

// POST /api/verify-code   body: { email, code }
app.post('/api/verify-code', (req, res) => {
  const { email, code } = req.body;
  if (!email || !code) {
    return res.status(400).json({ error: 'email and code are required' });
  }

  const key = email.toLowerCase();
  const pending = pendingCodes.get(key);

  if (!pending) return res.json({ valid: false, reason: 'no_code_sent' });
  if (Date.now() > pending.expiresAt) {
    pendingCodes.delete(key);
    return res.json({ valid: false, reason: 'expired' });
  }
  if (pending.code !== code.trim()) {
    return res.json({ valid: false, reason: 'wrong_code' });
  }

  pendingCodes.delete(key);
  return res.json({ valid: true });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`WordLy mailer listening on http://localhost:${PORT}`);
});
