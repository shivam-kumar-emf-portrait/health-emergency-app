const jwt = require("jsonwebtoken");
const User = require("../models/User");
const Hospital = require("../models/Hospital");

const authMiddleware = async (req, res, next) => {
  const token = req.headers.authorization?.split(" ")[1];

  if (!token) {
    return res.status(401).json({ message: "No token, authorization denied" });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);

    let account = null;

    if (decoded.role === "hospital_admin") {
      account = await Hospital.findById(decoded.id);
    } else {
      account = await User.findById(decoded.id);
    }

    if (!account) {
      return res.status(401).json({ message: "Account not found" });
    }

    req.user = account;
    req.userRole = decoded.role;

    next();
  } catch (err) {
    console.error("Auth Middleware Error:", err);
    res.status(401).json({ message: "Invalid token" });
  }
};

module.exports = authMiddleware;
