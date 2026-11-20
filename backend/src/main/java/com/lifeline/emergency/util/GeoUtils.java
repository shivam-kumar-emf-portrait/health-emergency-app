package com.lifeline.emergency.util;

public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates the great-circle distance between two geographic coordinates
     * using the Haversine formula.
     *
     * @param lat1 Latitude of origin point
     * @param lon1 Longitude of origin point
     * @param lat2 Latitude of destination point
     * @param lon2 Longitude of destination point
     * @return Distance in kilometers
     */
    public static double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return Math.round((EARTH_RADIUS_KM * c) * 100.0) / 100.0;
    }

    /**
     * Formats distance with unit (e.g. "2.4 km")
     */
    public static String formatDistance(double km) {
        return String.format("%.2f km", km);
    }

    /**
     * Rough urban emergency ambulance travel time calculation (assuming 35 km/h urban speed + 2 min dispatch)
     */
    public static String estimateTravelTime(double km) {
        if (km <= 0.1) {
            return "< 2 mins";
        }
        int minutes = (int) Math.ceil((km / 35.0) * 60.0) + 2;
        return minutes + " mins";
    }
}

