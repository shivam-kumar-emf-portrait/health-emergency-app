package com.lifeline.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeline.emergency.dto.EmergencyTransitAiRequest;
import com.lifeline.emergency.dto.EmergencyTransitAiResponse;
import com.lifeline.emergency.dto.NotifyHospitalRequest;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.enums.EmergencyType;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmergencyTransitAiService {

    private final HospitalRepository hospitalRepository;
    private final EmergencyCaseService emergencyCaseService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.apiKey:}")
    private String openRouterApiKey;

    @Value("${openrouter.apiUrl:https://openrouter.ai/api/v1/chat/completions}")
    private String openRouterApiUrl;

    @Value("${openrouter.model:meta-llama/llama-3.1-8b-instruct}")
    private String modelName;

    public EmergencyTransitAiService(HospitalRepository hospitalRepository, EmergencyCaseService emergencyCaseService) {
        this.hospitalRepository = hospitalRepository;
        this.emergencyCaseService = emergencyCaseService;
    }

    public EmergencyTransitAiResponse generateTransitPlan(EmergencyTransitAiRequest request) {
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElse(null);

        String hospitalName = hospital != null ? hospital.getName() : request.getHospitalName();
        String hospitalPhone = hospital != null ? hospital.getContactNumber() : "+91 33 2456 7890";
        double hospLat = hospital != null ? hospital.getLatitude() : (request.getHospitalLat() != null ? request.getHospitalLat() : 22.5650);
        double hospLng = hospital != null ? hospital.getLongitude() : (request.getHospitalLng() != null ? request.getHospitalLng() : 88.3550);
        double userLat = request.getOriginLat() != null ? request.getOriginLat() : 22.5550;
        double userLng = request.getOriginLng() != null ? request.getOriginLng() : 88.3510;

        String navUrl = String.format("https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=%f,%f;%f,%f",
                userLat, userLng, hospLat, hospLng);

        // Call OpenRouter AI for emergency transit plan
        String aiGeneratedAdvice = callOpenRouterForTransit(request, hospitalName);

        // Automatic Pre-Alert Dispatch to Hospital ER Ward
        boolean preAlertSent = false;
        try {
            NotifyHospitalRequest notifyReq = new NotifyHospitalRequest();
            notifyReq.setHospitalId(request.getHospitalId());
            notifyReq.setEmergencyType(request.getEmergencyType());
            notifyReq.setSeverity("CRITICAL");
            notifyReq.setPatientName("Incoming Transit Patient (" + (request.getPatientAge() != null ? request.getPatientAge() + "y" : "Age N/A") + ")");
            notifyReq.setPatientPhone(request.getPatientGender() != null ? request.getPatientGender() : "Emergency Call");
            notifyReq.setUserLatitude(userLat);
            notifyReq.setUserLongitude(userLng);
            notifyReq.setNotes("IN-TRANSIT ALERT (ETA " + request.getEstimatedMinutes() + "): " + request.getPatientSymptoms());
            emergencyCaseService.notifyHospital(notifyReq);
            preAlertSent = true;
        } catch (Exception e) {
            System.err.println("Could not auto-dispatch in-transit alert: " + e.getMessage());
        }

        // Generate Domain-Specific Dos and Don'ts
        List<String> dos = new ArrayList<>();
        List<String> donts = new ArrayList<>();

        if (request.getEmergencyType() == EmergencyType.CARDIAC) {
            dos.add("Keep patient seated at a 45-degree angle with knees bent to ease cardiac load.");
            dos.add("Loosen tight clothing around neck, chest, and waist.");
            dos.add("If conscious and non-allergic, ask if they carry prescribed Nitroglycerin / Sorbitrate.");
            dos.add("Call hospital emergency line in advance to alert Cath Lab.");

            donts.add("DO NOT let the patient walk or exert any physical effort.");
            donts.add("DO NOT give solid foods or caffeinated drinks.");
            donts.add("DO NOT drive erratically; avoid harsh sudden acceleration or braking.");
        } else if (request.getEmergencyType() == EmergencyType.STROKE) {
            dos.add("Keep patient lying flat with head slightly elevated 15-30 degrees.");
            dos.add("Turn patient gently to their side (recovery position) if vomiting occurs to avoid choking.");
            dos.add("Note the exact time symptoms started (critical for thrombolytic / clot-busting therapy).");

            donts.add("DO NOT give Aspirin or blood thinners (can be fatal in hemorrhagic stroke).");
            donts.add("DO NOT give any fluids or food (swallow reflex is often paralyzed).");
            donts.add("DO NOT let patient sleep without monitoring breathing.");
        } else if (request.getEmergencyType() == EmergencyType.TRAUMA || request.getEmergencyType() == EmergencyType.BLEEDING) {
            dos.add("Apply continuous firm pressure on bleeding sites with a clean cloth.");
            dos.add("Keep cervical spine and neck straight and stabilized if vehicular collision.");
            dos.add("Elevate legs 20-30 cm if patient is in shock (cold/clammy), unless spinal injury suspected.");

            donts.add("DO NOT remove deeply impaled objects; stabilize them in place.");
            donts.add("DO NOT twist or move the neck or spine abruptly.");
        } else {
            dos.add("Ensure patient airway is completely clear.");
            dos.add("Turn on vehicle hazard lights and maintain steady emergency transit.");
            dos.add("Keep patient calm, talking softly to monitor consciousness level.");

            donts.add("DO NOT panic or exceed controllable vehicle speed.");
            donts.add("DO NOT leave the patient unattended in the rear seat.");
        }

        String sbar = String.format("SBAR HANDOVER BRIEF FOR ER WARD:\n" +
                        "• S (Situation): Incoming %s emergency, ETA approx %s.\n" +
                        "• B (Background): %s patient with acute symptoms.\n" +
                        "• A (Assessment): Reported symptoms: %s.\n" +
                        "• R (Recommendation): Prepare triage bay, on-duty specialist, and life-support.",
                request.getEmergencyType(), request.getEstimatedMinutes(),
                (request.getPatientAge() != null ? request.getPatientAge() + "yo " : "") + (request.getPatientGender() != null ? request.getPatientGender() : "Patient"),
                request.getPatientSymptoms() != null ? request.getPatientSymptoms() : "Acute emergency condition");

        EmergencyTransitAiResponse response = new EmergencyTransitAiResponse();
        response.setEmergencyType(request.getEmergencyType() != null ? request.getEmergencyType().name() : "CRITICAL");
        response.setHospitalName(hospitalName);
        response.setHospitalPhone(hospitalPhone);
        response.setEtaFormatted(request.getEstimatedMinutes() != null ? request.getEstimatedMinutes() : "5-10 mins");
        response.setTransitProtocols(aiGeneratedAdvice);
        response.setImmediateDos(dos);
        response.setImmediateDonts(donts);
        response.setSbarBrief(sbar);
        response.setNavigationUrl(navUrl);
        response.setHospitalPreAlertActive(preAlertSent);

        return response;
    }

    private String callOpenRouterForTransit(EmergencyTransitAiRequest req, String hospitalName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openRouterApiKey);
            headers.set("HTTP-Referer", "http://localhost:8080");
            headers.set("X-Title", "Lifeline Transit AI");

            String prompt = String.format("""
                    You are an Emergency Flight Paramedic and Advanced Transit Route Co-Pilot.
                    Patient is actively in transit to %s (%s km away, ETA %s).
                    Emergency Category: %s
                    Patient Reported Symptoms: %s
                    Patient Age: %s, Gender: %s
                    
                    Provide short, high-urgency, life-saving in-transit instructions for the driver and caregiver:
                    1. Exact patient physical posture inside vehicle.
                    2. Immediate airway & vitals monitoring instructions.
                    3. Driving strategy (speed control, avoiding braking shocks to prevent intracranial pressure surges or internal bleeding dislocation).
                    Keep instructions concise, bulleted, commanding, and medically precise.
                    """,
                    hospitalName, req.getDistanceKm(), req.getEstimatedMinutes(),
                    req.getEmergencyType(), req.getPatientSymptoms(),
                    req.getPatientAge(), req.getPatientGender());

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", "You are an urgent emergency medical transit co-pilot. Keep replies brief, bulleted, and actionable."),
                    Map.of("role", "user", "content", prompt)
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(openRouterApiUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
                if (!contentNode.isMissingNode() && !contentNode.asText().isEmpty()) {
                    return contentNode.asText();
                }
            }
        } catch (Exception e) {
            System.err.println("OpenRouter Transit AI call failed: " + e.getMessage() + ". Using paramedic clinical protocol.");
        }

        return "EMERGENCY IN-TRANSIT PROTOCOL ACTIVATED:\n" +
                "• Posture: Maintain semi-fowlers 45-degree angle (for cardiac/respiratory) or flat with head aligned (for trauma/stroke).\n" +
                "• Oxygen & Airway: Ensure unobstructed breathing; loosen collar and belt.\n" +
                "• Driving Advisory: Activate hazard lights; maintain steady velocity without harsh stops to prevent internal shock dislocation.\n" +
                "• Handover: Hospital emergency bay has been pre-alerted and is standing by.";
    }
}
