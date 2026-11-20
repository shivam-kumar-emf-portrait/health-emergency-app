require("dotenv").config();

const mongoose = require("mongoose");
const Hospital = require("./models/Hospital");
const hospitals = require("./data/hospitals");

mongoose.connect(process.env.MONGO_URI)
  .then(async () => {
    await Hospital.deleteMany();
    await Hospital.insertMany(hospitals);
    console.log("Hospitals seeded");
    process.exit();
  })
  .catch(err => console.error(err));
