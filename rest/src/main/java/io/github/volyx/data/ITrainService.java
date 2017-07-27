package io.github.volyx.data;

import java.io.IOException;

public interface ITrainService {
    TrainSearchResult find(TrainFilter request) throws SearchException, IOException;
}
