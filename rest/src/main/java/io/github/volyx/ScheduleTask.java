package io.github.volyx;


import io.github.volyx.data.ITrainService;
import io.github.volyx.data.SearchException;
import io.github.volyx.data.TrainFilter;
import io.github.volyx.data.TrainSearchResult;
import io.github.volyx.data.providers.http.StationService;
import io.github.volyx.data.providers.http.TrainService;
import io.github.volyx.notification.INotificationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

public final class ScheduleTask extends TimerTask {
    private final INotificationService notificationService;
    private final ITrainService trainService;
    private final List<TrainFilter> filters;
    private final Log log = LogFactory.getLog(ScheduleTask.class);
    private final StationService stationService;

    public ScheduleTask(List<TrainFilter> filters, StationService stationService, INotificationService notificationService) throws IOException {
        this.stationService = stationService;
        this.trainService = new TrainService(stationService);
        this.notificationService = notificationService;
        this.filters = filters;
    }


    @Override
    public void run() {
        log.info("===========================================");
        for (TrainFilter filter : filters) {
            try {
                TrainSearchResult trainSearchResult = trainService.find(filter);
                if (!trainSearchResult.hasError()) {
                    if (trainSearchResult.getItems().size() > 0) {
                        notificationService.notifySuccess(filter, trainSearchResult);
                    }
                } else {
                    //notificationService.notifyServiceIsDown();
                }
                //notificationService.notifyServiceIsAvailable();

            } catch (SearchException | IOException e) {
                log.error(e);
            }
        }
    }
}
