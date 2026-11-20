package com.lifeline.emergency;

import com.lifeline.emergency.dto.EmergencySearchRequest;
import com.lifeline.emergency.dto.EmergencySearchResponse;
import com.lifeline.emergency.dto.HospitalScoreDto;
import com.lifeline.emergency.enums.EmergencyType;
import com.lifeline.emergency.service.EmergencyMatchingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EmergencyMatchingServiceTest {

    @Autowired
    private EmergencyMatchingService emergencyMatchingService;

    @Test
    @DisplayName("CARDIAC Emergency: Top 3 Nearest vs Top 3 Best Equipped with Cardiologist and ICU beds")
    void testCardiacEmergencyMatching() {
        // User located at central Kolkata (Park Street area: 22.5540, 88.3520)
        EmergencySearchRequest request = new EmergencySearchRequest(22.5540, 88.3520, EmergencyType.CARDIAC);

        EmergencySearchResponse response = emergencyMatchingService.findHospitalsForEmergency(request);

        assertNotNull(response);
        assertEquals(EmergencyType.CARDIAC, response.getEmergencyType());

        // Verify Top 3 Nearest
        List<HospitalScoreDto> nearest = response.getTopNearestHospitals();
        assertEquals(3, nearest.size(), "Should return exactly top 3 nearest hospitals");
        assertTrue(nearest.get(0).getDistanceKm() <= nearest.get(1).getDistanceKm(), "Nearest list must be sorted by distance ASC");
        assertTrue(nearest.get(1).getDistanceKm() <= nearest.get(2).getDistanceKm(), "Nearest list must be sorted by distance ASC");

        // Verify Top 3 Best
        List<HospitalScoreDto> best = response.getTopBestHospitals();
        assertEquals(3, best.size(), "Should return exactly top 3 best equipped hospitals");
        assertTrue(best.get(0).getOverallScore() >= best.get(1).getOverallScore(), "Best list must be sorted by score DESC");

        // Top 1 Best hospital for CARDIAC must have matching specialist on duty and ICU beds
        HospitalScoreDto topBest = best.get(0);
        assertTrue(topBest.isHasMatchingSpecialist(), "Top rated cardiac hospital must have a Cardiologist on duty");
        assertTrue(topBest.getAvailableICUBeds() > 0, "Top rated cardiac hospital must have available ICU beds");

        System.out.println("=== Top Nearest for Heart Emergency ===");
        nearest.forEach(h -> System.out.println(h.getName() + " -> " + h.getDistanceFormatted()));

        System.out.println("=== Top Best for Heart Emergency ===");
        best.forEach(h -> System.out.println(h.getName() + " -> Score: " + h.getOverallScore() + " | Highlights: " + h.getMatchHighlights()));
    }

    @Test
    @DisplayName("STROKE Emergency: Top Best prioritizes Neurologist on duty")
    void testStrokeEmergencyMatching() {
        EmergencySearchRequest request = new EmergencySearchRequest(22.5700, 88.3600, EmergencyType.STROKE);
        EmergencySearchResponse response = emergencyMatchingService.findHospitalsForEmergency(request);

        assertNotNull(response);
        List<HospitalScoreDto> best = response.getTopBestHospitals();
        assertFalse(best.isEmpty());

        // Check that at least one top hospital features a Neurologist on duty
        boolean hasNeuro = best.stream().anyMatch(h -> h.isHasMatchingSpecialist() &&
                h.getMatchingSpecialists().stream().anyMatch(s -> s.contains("Neurologist")));
        assertTrue(hasNeuro, "Stroke emergency should prioritize hospital with on-duty Neurologist");
    }
}

