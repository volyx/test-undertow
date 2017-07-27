package io.github.volyx.data.providers.selenium.pageobjects.results;

import java.util.Date;
import java.util.Map;


public class ResultsFormItem {
    private final String trainId;
    private final Date departs;
    private final Date arrives;
    private final Map<String, Integer> availableSeats;

    public ResultsFormItem(String trainId, Date departs, Date arrives, Map<String, Integer> availableSeats) {
        this.trainId = trainId;
        this.departs = departs;
        this.arrives = arrives;
        this.availableSeats = availableSeats;
    }

    public String getTrainId() {
        return trainId;
    }

    public Date getDeparts() {
        return departs;
    }

    public Date getArrives() {
        return arrives;
    }

    public Map<String, Integer> getAvailableSeats() {
        return availableSeats;
    }
}
