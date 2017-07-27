package io.github.volyx.data.providers.http;

import com.google.gson.Gson;
import io.github.volyx.data.SearchException;
import io.github.volyx.data.Station;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StationService {


    private final String STATION_URL = "https://pass.rzd.ru/suggester?&lang=ru&lat=0&compactMode=y&stationNamePart=%s";

    private final Map<String, Integer> stationCache = new HashMap<String, Integer>();

    private IWebClient webClient;

    private IWebClient getWebClient() {
        if (webClient == null) {
            webClient = new WebClient();
        }
        return webClient;
    }

    public int getStationId(String name) throws SearchException, IOException {
        if (stationCache.containsKey(name)) {
            return stationCache.get(name);
        }
        final Station[] stations = getStations(name);
        for (Station station : stations) {
            if (station.n.toLowerCase().equals(name.toLowerCase())) {
                int id = station.c;
                stationCache.put(name, id);
                return id;
            }
        }
        throw new SearchException("Station not found");
    }

    @Nullable
    private Station[] getStations(String name) throws IOException {
        name = name.toUpperCase();
        final URL url = new URL(String.format(STATION_URL, name.replace(" ", " ")));
        final String str = getWebClient().downloadString(url, UserAgent.DEFAULT);
        return new Gson().fromJson(str, Station[].class);
    }

    @Nonnull
    public List<String> getStationsName(String term) throws IOException {
        Station[] stations = getStations(term);
        if (stations == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(stations).map(s -> s.n).collect(Collectors.toList());
    }
}
