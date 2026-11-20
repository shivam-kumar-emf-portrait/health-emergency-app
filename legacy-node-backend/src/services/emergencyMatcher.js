const getDistance = require("../utils/distance");

function matchHospitals(hospitals, userLocation, emergencyType) {

  // 🔹 Step 1: Filter eligible hospitals
  const eligibleHospitals = hospitals.filter(h => {
    if (!h.isActive) return false;
    if (!h.isVerified) return false;
    if (!h.isOpen) return false;
    if (!h.hasEmergency) return false;
    if (h.availableBeds <= 0) return false;

    if (emergencyType === "CRITICAL" && h.availableICUBeds <= 0) {
      return false;
    }

    return true;
  });

  // 🔹 Step 2: Score hospitals
  const scored = eligibleHospitals.map(h => {
    const distance = getDistance(
      userLocation.lat,
      userLocation.lng,
      h.location.lat,
      h.location.lng
    );

    let score = 0;

    // Critical emergency scoring
    if (emergencyType === "CRITICAL") {
      score += 20; // priority boost
      score += h.availableICUBeds * 3;
      score += h.ventilatorsAvailable * 2;
      score += h.technologies?.length || 0;
    } else {
      // Non-critical
      score += h.availableBeds;
      score += 10 / (distance || 1);
    }

    return {
      ...h.toObject(),
      distance: distance.toFixed(2),
      score
    };
  });

  // 🔹 Step 3: Sort & limit
  return scored
    .sort((a, b) => b.score - a.score)
    .slice(0, 3);
}

module.exports = matchHospitals;
