package io.github.volyx.notification;


import io.github.volyx.data.Car;
import io.github.volyx.data.Train;
import io.github.volyx.data.TrainFilter;
import io.github.volyx.data.TrainSearchResult;
import net.openhft.chronicle.map.ChronicleMap;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TelegramNotificationService implements INotificationService {

    private final Log logger = LogFactory.getLog(TelegramNotificationService.class);

    private ChronicleMap<CharSequence, Car> cars = ChronicleMap
            .of(CharSequence.class, Car.class)
            .name("train-cars")
            .averageKey("123")
            .averageValueSize(50.0)
            .entries(50_000)
            .create();

    @Override
    public void notifySuccess(TrainFilter filter, TrainSearchResult result) {
        for (Train train : result.getItems()) {
            Car currentCar = train.getSeats().get(0);
            Car car = cars.get(train.getTrainId());

            if (car == null) {
                cars.put(train.getTrainId(), currentCar);
                continue;
            }

            if (currentCar.freeSeats < car.freeSeats) {
                System.out.println("Новые места " + train);
            }
        }
        System.out.println("Find tickets" + result);
    }

    @Override
    public void notifyServiceIsAvailable() {

    }

    @Override
    public void notifyServiceIsDown() {

    }

    @Override
    public Map<String, Car> getTrains() {
        Map<String, Car> map = new HashMap<>();
        for (Map.Entry<CharSequence, Car> entry : cars.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
        return map;
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
