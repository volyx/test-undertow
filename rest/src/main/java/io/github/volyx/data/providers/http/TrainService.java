package io.github.volyx.data.providers.http;

import io.github.volyx.data.Car;
import io.github.volyx.data.ITrainService;
import io.github.volyx.data.SearchException;
import io.github.volyx.data.Train;
import io.github.volyx.data.TrainFilter;
import io.github.volyx.data.TrainSearchResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class TrainService implements ITrainService {

    private final String SESSION_URL = "https://pass.rzd.ru/timetable/public/ru?STRUCTURE_ID=735&layer_id=5371&dir=0&tfl=3&checkSeats=1&st0=%s&code0=%d&dt0=%s&st1=%s&code1=%d&dt1=%s";
    private final String TICKETS_URL = SESSION_URL + "&rid=%d";
    private final Log log = LogFactory.getLog(TrainService.class);
    private IWebClient webClient;
    private final StationService stationService;

    public TrainService(StationService stationService) {
        this.stationService = stationService;
    }

    private IWebClient getWebClient() {
        if (webClient == null) {
            webClient = new WebClient();
        }
        return webClient;
    }

    @Override
    public TrainSearchResult find(TrainFilter filter) throws SearchException, IOException {
        JSONObject obj = null;
        int maxRetry = 10;
        long waitTimeout = 1000;
        for (int i = 0; i < maxRetry; i++) {
            try {
                RequestContext params = getRequestContext(filter);
                if (obj != null && "OK".equals(obj.getString("result"))) {
                    return parse(obj, filter);
                } else {
                    Thread.sleep(waitTimeout);
                    obj = getJson(filter, params);
                }
            } catch (JSONException je) {
                try {
                    // captcha workaround
                    // todo: use backoff
                    webClient = null;
                    Thread.sleep(60000);
                } catch (Exception ex) {
                }
                je.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new TrainSearchResult(true);
    }

    private JSONObject getJson(TrainFilter request, RequestContext params) throws IOException, SearchException {
        int from = stationService.getStationId(request.getFrom());
        int to = stationService.getStationId(request.getTo());
        URL url = new URL(String.format(TICKETS_URL, formatString(request.getFrom()), from, formatDate(request.getWhen()), formatString(request.getTo()), to, formatDate(request.getWhen()), params.getrId()));
        String searchResult = getWebClient().downloadString(url, UserAgent.DEFAULT);
        return new JSONObject(searchResult);
    }

    private TrainSearchResult parse(JSONObject obj, TrainFilter filter) {
        final List<Train> trains = new ArrayList<>();
        final JSONArray tr = obj.getJSONArray("tp");
        for (int i = 0; i < tr.length(); i++) {
            JSONObject item = tr.getJSONObject(i);
            JSONArray list = item.getJSONArray("list");
            for (int j = 0; j < list.length(); j++) {
                JSONObject trainItem = list.getJSONObject(j);
                List<Car> allSeats = parseSeatsByClass(trainItem.getJSONArray("cars"));
                String trainCode = trainItem.getString("number2");
                if (!filter.isFilteredByTrainCode() || trainCode.toLowerCase().contains(filter.getTrainCode().toLowerCase())) {
                    List<Car> filteredSeats = allSeats.stream().filter(c-> filter.getSeatTypes().contains(c.typeLoc)).collect(Collectors.toList());
                    if (filteredSeats.size() > 0) {
                        trains.add(new Train(trainCode, filteredSeats));
                    }
                }
            }
            log.info(String.format("[%s]: %d non-filtered, %d filtered trains", filter.toString(), list.length(), trains.size()));
        }
        return new TrainSearchResult(trains);
    }


    private List<Car> parseSeatsByClass(JSONArray arr) {
        List<Car> filtered = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Car car = new Car();
            car.typeLoc = obj.getString("typeLoc");
            car.tariff = obj.getInt("tariff");
            car.freeSeats = obj.getInt("freeSeats");
            filtered.add(car);
        }
        return filtered;
    }


    private RequestContext getRequestContext(TrainFilter request) throws SearchException, IOException {
        int toStation = stationService.getStationId(request.getTo());
        int fromStation = stationService.getStationId(request.getFrom());
        URL url = new URL(String.format(SESSION_URL, formatString(request.getFrom()), fromStation, formatDate(request.getWhen()), formatString(request.getTo()), toStation, formatDate(request.getWhen())));
        String str = getWebClient().downloadString(url, UserAgent.FIREFOX);
        JSONObject json = new JSONObject(str);
        return new RequestContext(json.getLong("rid"));
    }

    private String formatString(String str) {
        return str.replace(" ", "%20");
    }

    private String formatDate(Date date) {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        return df.format(date);
    }

    private class RequestContext {
        private final long rId;

        public RequestContext(long rId) {
            this.rId = rId;
        }

        public long getrId() {
            return rId;
        }
    }
}
