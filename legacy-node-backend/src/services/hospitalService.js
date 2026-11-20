const Hospital = require("../models/Hospital");

exports.findBestHospital = async () => {
  return Hospital.find({});
};
