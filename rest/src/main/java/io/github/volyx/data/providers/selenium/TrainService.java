package io.github.volyx.data.providers.selenium;

import io.github.volyx.data.ITrainService;
import io.github.volyx.data.Train;
import io.github.volyx.data.TrainFilter;
import io.github.volyx.data.TrainSearchResult;
import io.github.volyx.data.providers.selenium.pageobjects.HomePage;
import io.github.volyx.data.providers.selenium.pageobjects.ResultsPage;
import io.github.volyx.data.providers.selenium.pageobjects.results.ResultsFormItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TrainService implements ITrainService {
    private final Log log = LogFactory.getLog(TrainService.class);
    private WebDriver webDriver;

    @Override
    public TrainSearchResult find(TrainFilter filter) throws IOException {
        log.debug("search filter");
        WebDriver driver = getWebDriver();
        HomePage home = new HomePage(driver);
        try {
            ResultsPage results = home.fillSearchForm(filter.getFrom(), filter.getTo(), filter.getWhen());
            List<ResultsFormItem> result = results.parse();
            List<Train> trains = new ArrayList<Train>();
            for (ResultsFormItem r : result) {
                String trainCode = r.getTrainId();
                if (!filter.isFilteredByTrainCode() || trainCode.toLowerCase().contains(filter.getTrainCode().toLowerCase())) {
//                    Map<String, Integer> filteredSeats = filterMap(r.getAvailableSeats(), filter.getSeatTypes());
//                    if (filteredSeats.size() > 0) {
//                        trains.add(new Train(trainCode, filteredSeats));
//                    }
                }
            }
            log.info(result.size() + " non-filtered, " + trains.size() + " filtered train results has been found");
            return new TrainSearchResult(trains);
        } catch (Exception ex) {
            return new TrainSearchResult(true);
        }
    }

    private WebDriver getWebDriver() throws IOException {
        if (webDriver == null) {
            webDriver = WebDriverFactory.create();
        }
        return webDriver;
    }

    private Map<String, Integer> filterMap(Map<String, Integer> target, List<String> filter) {
        if (filter.size() == 0) {
            return target;
        }
        Map<String, Integer> filtered = new HashMap<String, Integer>();
        for (String item : filter) {
            if (target.containsKey(item) && target.get(item) > 0) {
                filtered.put(item, target.get(item));
            }
        }
        return filtered;
    }
}
