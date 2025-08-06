package com.martysh12.racecs.net;

import com.martysh12.racecs.RaceCS;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StationManager {
    private static Map<String, String> stations = new HashMap<>();

    public static void downloadStations() {
        String locale = RaceCS.mc.getLanguageManager().getLanguage();
        String[] localeParts = locale.split("_");

        if (localeParts.length != 2) {
            RaceCS.logger.error("Unable to convert locale {} to i18next representation, aborting download.", locale);
            return;
        }

        String convertedLocale = localeParts[0] + "-" + localeParts[1].toUpperCase(Locale.ROOT);

        new Thread(() -> {
            RaceCS.logger.info("Starting download of stations with locale " + convertedLocale);
            Map<String, String> stationMap = APIUtils.getStations(convertedLocale);
            if (stationMap == null) {
                RaceCS.logger.error("Unable to download the stations. Keeping last map.");
                return;
            }

            stations = stationMap;
            RaceCS.logger.info("Downloaded stations successfully. {} stations loaded.", stationMap.size());
        }, "Station Download Thread").start();
    }

    public static String getStationFullName(String shortName) {
        return stations.get(shortName);
    }

    public static List<String> getStations() {
        return List.copyOf(stations.keySet());
    }
}
