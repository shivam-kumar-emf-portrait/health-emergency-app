module.exports = [
  {
    name: "City Care Hospital",
    location: { lat: 22.5726, lng: 88.3639 },
    hasICU: true,
    icuBedsAvailable: 3,
    technologies: ["CT", "Ventilator"],
    bloodAvailable: ["A+", "O+", "B+"],
    contactNumber: "9876543210"
  },
  {
    name: "GreenLife Medical Center",
    location: { lat: 22.5800, lng: 88.3700 },
    hasICU: false,
    icuBedsAvailable: 0,
    technologies: ["X-Ray"],
    bloodAvailable: ["O+"],
    contactNumber: "9123456780"
  },
  {
    name: "Metro Emergency Hospital",
    location: { lat: 22.5650, lng: 88.3550 },
    hasICU: true,
    icuBedsAvailable: 5,
    technologies: ["CT", "MRI", "Ventilator"],
    bloodAvailable: ["A+", "A-", "O+", "O-"],
    contactNumber: "9988776655"
  }
];
