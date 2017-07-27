package io.github.volyx.data;

import java.util.LinkedList;
import java.util.List;


public class TrainSearchResult {
    private final Boolean isError;
    private final List<Train> items;

    public TrainSearchResult(boolean isError) {
        this.isError = isError;
        this.items = new LinkedList<Train>();
    }

    public TrainSearchResult(List<Train> items) {
        this.items = items;
        this.isError = false;
    }

    public boolean hasError() {
        return isError;
    }

    public List<Train> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "TrainSearchResult{" +
                "isError=" + isError +
                ", items=" + items +
                '}';
    }
}
