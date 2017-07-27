package io.github.volyx.notification;

import io.github.volyx.data.Car;
import io.github.volyx.data.TrainFilter;
import io.github.volyx.data.TrainSearchResult;
import net.openhft.chronicle.map.ChronicleMap;

import java.util.Map;


public interface INotificationService {
    void notifySuccess(TrainFilter filter, TrainSearchResult result);

    void notifyServiceIsAvailable();

    void notifyServiceIsDown();

    Map<String, Car> getTrains();
}
