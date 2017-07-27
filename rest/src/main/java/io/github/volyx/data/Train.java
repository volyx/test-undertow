package io.github.volyx.data;

import java.util.List;
import java.util.Map;


public final class Train {
    private final String trainId;
    private final List<Car> seats;

    public Train(String trainId, List<Car> seatsByClass) {
        this.trainId = trainId;
        this.seats = seatsByClass;
    }

    public long getSeatsByClass(String className) {
        return seats.stream().filter(c->c.typeLoc.equals(className)).count();
    }

    public List<Car> getSeats() {
        return seats;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(trainId + " ");
        for (Car car : seats) {
            sb.append(car.typeLoc + ":" + car.tariff + " руб " + car.freeSeats + " мест");
        }
        return sb.toString();
    }

    public String getTrainId() {
        return trainId;
    }
}
