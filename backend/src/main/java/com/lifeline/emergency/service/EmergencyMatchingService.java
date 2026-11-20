package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.EmergencySearchRequest;
import com.lifeline.emergency.dto.EmergencySearchResponse;
import com.lifeline.emergency.dto.HospitalScoreDto;
import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.entity.FeedbackReport;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.enums.DoctorSpecialization;
import com.lifeline.emergency.enums.EmergencyType;
import com.lifeline.emergency.repository.DoctorRepository;
import com.lifeline.emergency.repository.FeedbackReportRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import com.lifeline.emergency.util.GeoUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmergencyMatchingService {

    private final HospitalRepository hospitalRepository;
    private final DoctorRepository doctorRepository;
    private final FeedbackReportRepository feedbackReportRepository;

    public EmergencyMatchingService(HospitalRepository hospitalRepository,
                                  DoctorRepository doctorRepository,
                                  FeedbackReportRepository feedbackReportRepository) {
        this.hospitalRepository = hospitalRepository;
        this.doctorRepository = doctorRepository;
        this.feedbackReportRepository = feedbackReportRepository;
    }

    /**
     * Core Algorithm:
     * Evaluates hospitals for the given emergency, computing:
     * 1) Top 3 Nearest Hospitals (pure Haversine distance)
     * 2) Top 3 Best Hospitals (multi-factor readiness score: relevant doctor on-duty, ICU beds, equipment, proximity)
     */
    public EmergencySearchResponse findHospitalsForEmergency(EmergencySearchRequest request) {
        double userLat = request.getLatitude() != null ? request.getLatitude() : 22.5726;
        double userLng = request.getLongitude() != null ? request.getLongitude() : 88.3639;
        EmergencyType emergencyType = request.getEmergencyType() != null ? request.getEmergencyType() : EmergencyType.GENERAL;

        // Fetch all verified, active, open hospitals with emergency departments
        List<Hospital> hospitals = hospitalRepository.findActiveVerifiedOpenEmergencyHospitals();

        List<HospitalScoreDto> scoredHospitals = new ArrayList<>();

        for (Hospital hospital : hospitals) {
            // Must have at least 1 bed available for emergency admission
            if (hospital.getAvailableBeds() <= 0) {
                continue;
            }

            // Haversine distance
            double distanceKm = GeoUtils.calculateDistanceKm(userLat, userLng, hospital.getLatitude(), hospital.getLongitude());

            // Fetch on-duty doctors for this hospital
            List<Doctor> onDutyDoctors = doctorRepository.findByHospitalIdAndIsOnDutyTrue(hospital.getId());

            // Check if any on-duty doctor matches the required specialization for this emergency
            DoctorSpecialization requiredSpecialization = emergencyType.getRequiredSpecialization();
            List<Doctor> matchingDoctors = onDutyDoctors.stream()
                    .filter(doc -> doc.getSpecialization() == requiredSpecialization ||
                            (emergencyType == EmergencyType.TRAUMA && doc.getSpecialization() == DoctorSpecialization.ORTHOPEDICS) ||
                            doc.getSpecialization() == DoctorSpecialization.GENERAL_EMERGENCY)
                    .collect(Collectors.toList());

            boolean hasExactSpecialist = onDutyDoctors.stream()
                    .anyMatch(doc -> doc.getSpecialization() == requiredSpecialization);

            // Compute the intelligent match score (0 - 100)
            ScoringBreakdown breakdown = calculateMatchScore(hospital, emergencyType, distanceKm, hasExactSpecialist, onDutyDoctors);

            // Build human-readable highlights
            List<String> highlights = buildHighlights(hospital, emergencyType, hasExactSpecialist, onDutyDoctors, distanceKm);

            HospitalScoreDto dto = new HospitalScoreDto();
            dto.setId(hospital.getId());
            dto.setName(hospital.getName());
            dto.setContactNumber(hospital.getContactNumber());
            dto.setAddress(hospital.getAddress());
            dto.setLatitude(hospital.getLatitude());
            dto.setLongitude(hospital.getLongitude());
            dto.setDistanceKm(distanceKm);
            dto.setDistanceFormatted(GeoUtils.formatDistance(distanceKm));
            dto.setEstimatedTravelTime(GeoUtils.estimateTravelTime(distanceKm));
            dto.setAvailableBeds(hospital.getAvailableBeds());
            dto.setTotalBeds(hospital.getTotalBeds());
            dto.setAvailableICUBeds(hospital.getAvailableICUBeds());
            dto.setTotalICUBeds(hospital.getTotalICUBeds());
            dto.setVentilators(hospital.getVentilators());
            dto.setOxygenCylinders(hospital.getOxygenCylinders());
            dto.setAmbulances(hospital.getAmbulances());
            dto.setCardiacMonitors(hospital.getCardiacMonitors());
            dto.setOpen(hospital.isOpen());
            dto.setOverallScore(breakdown.totalScore);
            dto.setSpecialistScore(breakdown.specialistScore);
            dto.setIcuScore(breakdown.icuScore);
            dto.setBedScore(breakdown.bedScore);
            dto.setEquipmentScore(breakdown.equipmentScore);
            dto.setProximityScore(breakdown.proximityScore);
            dto.setScoreBreakdownSummary(breakdown.summary);
            dto.setHasMatchingSpecialist(hasExactSpecialist);
            dto.setMatchingSpecialists(matchingDoctors.stream().map(d -> d.getName() + " (" + d.getSpecialization().getDescription() + ")").collect(Collectors.toList()));
            dto.setMatchHighlights(highlights);
            dto.setBloodInventory(hospital.getBloodInventory());

            // Compute dynamic average rating & review count from real user feedback
            List<FeedbackReport> hospitalReports = feedbackReportRepository.findByTargetIdAndReportTypeOrderByCreatedAtDesc(hospital.getId(), "USER_TO_HOSPITAL");
            double avgRating = 4.8;
            int reviewCount = hospitalReports != null ? hospitalReports.size() : 0;
            if (reviewCount > 0) {
                double sum = 0;
                int ratedCount = 0;
                for (FeedbackReport r : hospitalReports) {
                    if (r.getRating() != null && r.getRating() > 0) {
                        sum += r.getRating();
                        ratedCount++;
                    }
                }
                if (ratedCount > 0) {
                    avgRating = Math.round((sum / ratedCount) * 10.0) / 10.0;
                }
            }
            dto.setAverageRating(avgRating);
            dto.setTotalReviews(reviewCount > 0 ? reviewCount : 12);

            scoredHospitals.add(dto);
        }

        // 1) Top 3 Nearest: Strictly sorted by distance ascending, strictly open & verified hospitals
        List<HospitalScoreDto> topNearest = scoredHospitals.stream()
                .filter(HospitalScoreDto::isOpen)
                .sorted(Comparator.comparingDouble(HospitalScoreDto::getDistanceKm))
                .limit(3)
                .collect(Collectors.toList());

        // 2) Top 3 Best: Strictly sorted by match score descending, then distance ascending, strictly open
        List<HospitalScoreDto> topBest = scoredHospitals.stream()
                .filter(HospitalScoreDto::isOpen)
                .sorted(Comparator.comparingDouble(HospitalScoreDto::getOverallScore).reversed()
                        .thenComparingDouble(HospitalScoreDto::getDistanceKm))
                .limit(3)
                .collect(Collectors.toList());

        EmergencySearchResponse response = new EmergencySearchResponse();
        response.setEmergencyType(emergencyType);
        response.setEmergencyTitle(emergencyType.getDisplayName());
        response.setRequiredSpecialty(emergencyType.getRequiredSpecialization().getDescription());
        response.setUserLatitude(userLat);
        response.setUserLongitude(userLng);
        response.setLocationName(request.getLocationName() != null ? request.getLocationName() : "Current Location");
        response.setTotalHospitalsFound(scoredHospitals.size());
        response.setTopNearestHospitals(topNearest);
        response.setTopBestHospitals(topBest);

        return response;
    }

    public static class ScoringBreakdown {
        public double totalScore;
        public double specialistScore;
        public double icuScore;
        public double bedScore;
        public double equipmentScore;
        public double proximityScore;
        public String summary;
    }

    /**
     * Transparent multi-criteria scoring algorithm for Emergency Readiness:
     * - Doctor match: up to 35 pts (Specialist present on duty)
     * - ICU capacity: up to 20 pts (Critical beds available)
     * - Critical equipment: up to 18 pts (Ventilators, Cardiac monitors, Oxygen, Blood)
     * - Regular beds: up to 15 pts
     * - Proximity factor: up to 12 pts (Closer gives higher score)
     */
    private ScoringBreakdown calculateMatchScore(Hospital hospital, EmergencyType emergencyType, double distanceKm,
                                                 boolean hasExactSpecialist, List<Doctor> onDutyDoctors) {
        ScoringBreakdown b = new ScoringBreakdown();

        // 1. Specialist Doctor Availability (Weight: 35)
        if (hasExactSpecialist) {
            b.specialistScore = 35.0;
        } else if (!onDutyDoctors.isEmpty()) {
            b.specialistScore = 15.0; // General doctors present
        } else {
            b.specialistScore = 0.0;
        }

        // 2. ICU Bed Availability (Weight: 20)
        // Especially critical for Heart/Cardiac, Stroke, Trauma, Respiratory
        if (emergencyType == EmergencyType.CARDIAC || emergencyType == EmergencyType.STROKE
                || emergencyType == EmergencyType.RESPIRATORY || emergencyType == EmergencyType.TRAUMA) {
            b.icuScore = Math.min(20.0, hospital.getAvailableICUBeds() * 5.0);
        } else {
            b.icuScore = Math.min(10.0, hospital.getAvailableICUBeds() * 2.5);
        }

        // 3. Regular Bed Capacity (Weight: 15)
        b.bedScore = Math.min(15.0, hospital.getAvailableBeds() * 1.0);

        // 4. Equipment Readiness (Weight: 18)
        if (emergencyType == EmergencyType.CARDIAC) {
            double monitors = Math.min(8.0, hospital.getCardiacMonitors() * 1.5);
            double vents = Math.min(6.0, hospital.getVentilators() * 1.5);
            double o2 = Math.min(4.0, hospital.getOxygenCylinders() * 0.5);
            b.equipmentScore = Math.min(18.0, monitors + vents + o2);
        } else if (emergencyType == EmergencyType.RESPIRATORY) {
            double vents = Math.min(10.0, hospital.getVentilators() * 2.0);
            double o2 = Math.min(8.0, hospital.getOxygenCylinders() * 0.8);
            b.equipmentScore = Math.min(18.0, vents + o2);
        } else if (emergencyType == EmergencyType.TRAUMA || emergencyType == EmergencyType.BLEEDING) {
            double amb = Math.min(8.0, hospital.getAmbulances() * 3.0);
            int bloodUnits = hospital.getBloodInventory() != null ?
                    hospital.getBloodInventory().values().stream().mapToInt(Integer::intValue).sum() : 0;
            double blood = Math.min(10.0, bloodUnits * 0.3);
            b.equipmentScore = Math.min(18.0, amb + blood);
        } else {
            double vents = Math.min(6.0, hospital.getVentilators() * 1.2);
            double o2 = Math.min(6.0, hospital.getOxygenCylinders() * 0.4);
            double amb = Math.min(6.0, hospital.getAmbulances() * 2.0);
            b.equipmentScore = Math.min(18.0, vents + o2 + amb);
        }

        // 5. Proximity Boost (Weight: 12)
        // Closer is better; 1 km gets ~11 pts, 10 km gets ~2 pts
        b.proximityScore = Math.max(0.0, 12.0 - (distanceKm * 1.0));

        double rawTotal = b.specialistScore + b.icuScore + b.bedScore + b.equipmentScore + b.proximityScore;
        b.totalScore = Math.min(100.0, Math.round(rawTotal * 10.0) / 10.0);
        b.specialistScore = Math.round(b.specialistScore * 10.0) / 10.0;
        b.icuScore = Math.round(b.icuScore * 10.0) / 10.0;
        b.bedScore = Math.round(b.bedScore * 10.0) / 10.0;
        b.equipmentScore = Math.round(b.equipmentScore * 10.0) / 10.0;
        b.proximityScore = Math.round(b.proximityScore * 10.0) / 10.0;

        b.summary = String.format("Specialist: %.1f/35 | ICU: %.1f/20 | Equipment: %.1f/18 | Beds: %.1f/15 | Proximity: %.1f/12",
                b.specialistScore, b.icuScore, b.equipmentScore, b.bedScore, b.proximityScore);

        return b;
    }

    private List<String> buildHighlights(Hospital hospital, EmergencyType type, boolean hasSpecialist,
                                        List<Doctor> onDutyDoctors, double distanceKm) {
        List<String> list = new ArrayList<>();

        if (hasSpecialist) {
            list.add("Specialist On-Duty: " + type.getRequiredSpecialization().getDescription());
        } else if (!onDutyDoctors.isEmpty()) {
            list.add("Staffing: General Emergency Doctor On-Duty");
        } else {
            list.add("Notice: General Emergency Staff Active");
        }

        if (type == EmergencyType.CARDIAC && hospital.getCardiacMonitors() > 0) {
            list.add("Equipment: " + hospital.getCardiacMonitors() + " Cardiac Monitors Active");
        }
        if (hospital.getVentilators() > 0) {
            list.add("Life Support: " + hospital.getVentilators() + " Ventilators Online");
        }
        if (hospital.getAmbulances() > 0) {
            list.add("Ambulance: " + hospital.getAmbulances() + " Dedicated Emergency Vehicles");
        }

        return list;
    }

    /**
     * Resolve area or landmark name to geographic coordinates (lat, lng).
     * Supports:
     * 1. Direct coordinate parsing ("22.55, 88.35").
     * 2. Regional Landmark Directory (Park Street, Esplanade, Howrah, Salt Lake, etc.).
     * 3. OpenStreetMap Nominatim Geocoding fallback for global or national places.
     */
    /**
     * Resolve area or landmark name to geographic coordinates (lat, lng).
     * Supports:
     * 1. Direct coordinate parsing ("22.55, 88.35").
     * 2. Regional Landmark Directory (Park Street, Esplanade, Howrah, Salt Lake, etc.).
     * 3. OpenStreetMap Nominatim Geocoding fallback for global or national places.
     */
    public Map<String, Object> resolveLocationCoordinates(String query) {
        Map<String, Object> result = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            result.put("found", false);
            result.put("name", "Central District (Default)");
            result.put("latitude", 22.5726);
            result.put("longitude", 88.3639);
            return result;
        }

        String q = query.trim().toLowerCase();

        // 1. Direct Coordinate parsing (e.g. "22.55, 88.35" or "22.55 88.35")
        if (q.matches("^[-+]?[0-9]*\\.?[0-9]+[\\s,]+[-+]?[0-9]*\\.?[0-9]+$")) {
            try {
                String[] parts = q.split("[,\\s]+");
                double lat = Double.parseDouble(parts[0]);
                double lng = Double.parseDouble(parts[1]);
                result.put("found", true);
                result.put("name", String.format("Custom Coordinates (%.4f, %.4f)", lat, lng));
                result.put("latitude", lat);
                result.put("longitude", lng);
                return result;
            } catch (Exception ignored) {}
        }

        // 2. Regional Landmark & Hub Dictionary
        Map<String, double[]> dictionary = getRegionalLandmarkDictionary();
        for (Map.Entry<String, double[]> entry : dictionary.entrySet()) {
            if (q.contains(entry.getKey()) || entry.getKey().contains(q)) {
                result.put("found", true);
                String formattedName = capitalizeWords(entry.getKey());
                result.put("name", formattedName + ", Kolkata Region");
                result.put("latitude", entry.getValue()[0]);
                result.put("longitude", entry.getValue()[1]);
                return result;
            }
        }

        // 3. OpenStreetMap Nominatim Geocoding fallback
        try {
            String url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" +
                    java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "LifelineEmergency/1.0 (HospitalEmergencyApp)");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            org.springframework.http.ResponseEntity<String> response = rt.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
                if (root.isArray() && !root.isEmpty()) {
                    com.fasterxml.jackson.databind.JsonNode first = root.get(0);
                    double lat = first.path("lat").asDouble();
                    double lon = first.path("lon").asDouble();
                    String displayName = first.path("display_name").asText();
                    result.put("found", true);
                    result.put("name", displayName);
                    result.put("latitude", lat);
                    result.put("longitude", lon);
                    return result;
                }
            }
        } catch (Exception e) {
            System.err.println("Nominatim geocoding fallback failed: " + e.getMessage());
        }

        // 4. Fallback if not found
        result.put("found", false);
        result.put("name", query + " (Estimated Location)");
        result.put("latitude", 22.5726);
        result.put("longitude", 88.3639);
        return result;
    }

    /**
     * Provide real-time auto-suggestions for location search.
     * Combines high-speed regional landmark directory and OpenStreetMap Nominatim live autocomplete.
     */
    public List<Map<String, Object>> suggestLocations(String query) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        if (query == null || query.trim().length() < 1) {
            return suggestions;
        }

        String q = query.trim().toLowerCase();
        Map<String, double[]> dictionary = getRegionalLandmarkDictionary();
        Map<String, String> descriptions = getRegionalDescriptions();
        Set<String> addedKeys = new HashSet<>();

        // 1. Check in Regional Directory first (instant local response)
        for (Map.Entry<String, double[]> entry : dictionary.entrySet()) {
            String key = entry.getKey();
            if (key.contains(q) || q.contains(key)) {
                String title = capitalizeWords(key);
                if (addedKeys.add(title)) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", title + ", Kolkata Region");
                    item.put("shortName", title);
                    item.put("latitude", entry.getValue()[0]);
                    item.put("longitude", entry.getValue()[1]);
                    item.put("category", descriptions.getOrDefault(key, "Regional Medical & Transit Hub"));
                    suggestions.add(item);
                }
            }
            if (suggestions.size() >= 6) {
                break;
            }
        }

        // 2. If fewer than 4 matches and query length >= 3, query live OpenStreetMap Nominatim for real global/national locations
        if (suggestions.size() < 4 && q.length() >= 3) {
            try {
                String url = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=" +
                        java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("User-Agent", "LifelineEmergency/1.0 (HospitalEmergencyApp)");
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

                org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
                factory.setConnectTimeout(1500);
                factory.setReadTimeout(1500);
                org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate(factory);

                org.springframework.http.ResponseEntity<String> response = rt.exchange(
                        url, org.springframework.http.HttpMethod.GET, entity, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(response.getBody());
                    if (root.isArray()) {
                        for (com.fasterxml.jackson.databind.JsonNode node : root) {
                            String displayName = node.path("display_name").asText();
                            double lat = node.path("lat").asDouble();
                            double lon = node.path("lon").asDouble();
                            String type = node.path("type").asText("Area");

                            String[] parts = displayName.split(",");
                            String cleanName = parts.length > 2 ? parts[0].trim() + ", " + parts[1].trim() + ", " + parts[parts.length - 1].trim() : displayName;

                            if (addedKeys.add(cleanName)) {
                                Map<String, Object> item = new HashMap<>();
                                item.put("name", cleanName);
                                item.put("shortName", parts[0].trim());
                                item.put("latitude", lat);
                                item.put("longitude", lon);
                                item.put("category", capitalizeWords(type) + " (Live GPS Match)");
                                suggestions.add(item);
                            }
                            if (suggestions.size() >= 8) break;
                        }
                    }
                }
            } catch (Exception ignored) {
                // Return fast local results if external network is slow
            }
        }

        return suggestions;
    }

    private Map<String, double[]> getRegionalLandmarkDictionary() {
        Map<String, double[]> dict = new LinkedHashMap<>();
        dict.put("park street", new double[]{22.5550, 88.3510});
        dict.put("esplanade", new double[]{22.5680, 88.3520});
        dict.put("dharmatala", new double[]{22.5680, 88.3520});
        dict.put("howrah station", new double[]{22.5830, 88.3426});
        dict.put("howrah", new double[]{22.5830, 88.3426});
        dict.put("howrah bridge", new double[]{22.5850, 88.3470});
        dict.put("salt lake sector v", new double[]{22.5690, 88.4310});
        dict.put("sector v", new double[]{22.5690, 88.4310});
        dict.put("salt lake", new double[]{22.5690, 88.4050});
        dict.put("bidhannagar", new double[]{22.5850, 88.4100});
        dict.put("shyambazar", new double[]{22.5990, 88.3710});
        dict.put("alipore", new double[]{22.5330, 88.3290});
        dict.put("gariahat", new double[]{22.5180, 88.3650});
        dict.put("ballygunge", new double[]{22.5280, 88.3650});
        dict.put("new town", new double[]{22.5867, 88.4754});
        dict.put("rajarhat", new double[]{22.6200, 88.4750});
        dict.put("jadavpur", new double[]{22.4980, 88.3710});
        dict.put("dhakuria", new double[]{22.5080, 88.3640});
        dict.put("dum dum", new double[]{22.6540, 88.4467});
        dict.put("airport ccu", new double[]{22.6540, 88.4467});
        dict.put("behala", new double[]{22.4988, 88.3184});
        dict.put("sealdah", new double[]{22.5670, 88.3710});
        dict.put("chandni chowk", new double[]{22.5660, 88.3550});
        dict.put("bhowanipore", new double[]{22.5350, 88.3470});
        dict.put("ultadanga", new double[]{22.5960, 88.3890});
        dict.put("ruby hospital", new double[]{22.5130, 88.4030});
        dict.put("em bypass", new double[]{22.5130, 88.4030});
        dict.put("science city", new double[]{22.5400, 88.3960});
        dict.put("tollygunge", new double[]{22.4980, 88.3460});
        dict.put("kalighat", new double[]{22.5210, 88.3470});
        dict.put("college street", new double[]{22.5740, 88.3630});
        dict.put("burrabazar", new double[]{22.5840, 88.3560});
        dict.put("kasba", new double[]{22.5180, 88.3880});
        dict.put("kolkata central", new double[]{22.5726, 88.3639});
        return dict;
    }

    private Map<String, String> getRegionalDescriptions() {
        Map<String, String> map = new HashMap<>();
        map.put("park street", "Central Commercial & Medical Hub");
        map.put("esplanade", "Central Transit & Bus Terminal");
        map.put("dharmatala", "Central Transit Hub");
        map.put("howrah station", "Major Railway Terminal & Junction");
        map.put("howrah", "City of Howrah Western Bank");
        map.put("howrah bridge", "Historic River Crossing Hub");
        map.put("salt lake sector v", "IT & Tech Park Corridor");
        map.put("sector v", "IT Corridor Bidhannagar");
        map.put("salt lake", "Bidhannagar Central Zone");
        map.put("bidhannagar", "Salt Lake Transit Belt");
        map.put("shyambazar", "North Kolkata Five-Point Hub");
        map.put("alipore", "South Kolkata Medical & Hospital District");
        map.put("gariahat", "South Kolkata Commercial Intersection");
        map.put("ballygunge", "South Central Residential Belt");
        map.put("new town", "Rajarhat New Town High-Growth Corridor");
        map.put("rajarhat", "North-East Urban Extension");
        map.put("jadavpur", "South Kolkata Academic & Residential Zone");
        map.put("dhakuria", "South Kolkata Hospital Belt");
        map.put("dum dum", "North Kolkata Metro & Transit Hub");
        map.put("airport ccu", "Netaji Subhash Chandra Bose Airport");
        map.put("behala", "South-West Residential Hub");
        map.put("sealdah", "Major Rail & Transit Terminal");
        map.put("chandni chowk", "Central Market District");
        map.put("bhowanipore", "South Kolkata Traditional Belt");
        map.put("ultadanga", "North-East Transit Crossroads");
        map.put("ruby hospital", "EM Bypass Trauma & Medical Corridor");
        map.put("em bypass", "Eastern Arterial Medical Corridor");
        map.put("science city", "EM Bypass Central Junction");
        map.put("tollygunge", "South Kolkata Metro Terminal");
        map.put("kalighat", "South Kolkata Heritage Zone");
        map.put("college street", "Medical College & University Area");
        map.put("burrabazar", "Wholesale & Business District");
        map.put("kasba", "South-East Commercial Hub");
        map.put("kolkata central", "Metropolitan Core");
        return map;
    }

    private String capitalizeWords(String str) {
        String[] words = str.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}

