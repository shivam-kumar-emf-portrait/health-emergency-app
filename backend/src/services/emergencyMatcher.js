const getDistance = require("../utils/distance");

function matchHospitals(hospitals, userLocation, emergencyType) {

  const scored = hospitals.map(h => {
    const distance = getDistance(
      userLocation.lat,
      userLocation.lng,
      h.location.lat,
      h.location.lng
    );

    let score = 0;

    if (emergencyType === "CRITICAL") {
      if (h.hasICU) score += 10;
      score += h.icuBedsAvailable * 2;
      score += h.technologies.length;
      score += h.bloodAvailable.length;
    } else {
      score += 10 / (distance || 1);
    }

    return {
      ...h.toObject(),
      distance: distance.toFixed(2),
      score
    };
  });

  return scored
    .sort((a, b) => b.score - a.score)
    .slice(0, 3);
}

module.exports = matchHospitals;
