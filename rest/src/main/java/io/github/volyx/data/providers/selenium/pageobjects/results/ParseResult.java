package io.github.volyx.data.providers.selenium.pageobjects.results;

import java.util.ArrayList;
import java.util.List;


public class ParseResult {
    private final boolean isInProgress;
    private final List<ResultsFormItem> trains;

    public ParseResult(List<ResultsFormItem> trains) {
        this.trains = trains;
        this.isInProgress = false;
    }

    public ParseResult(Boolean isInProgress) {
        this.trains = new ArrayList<ResultsFormItem>();
        this.isInProgress = isInProgress;
    }

    public List<ResultsFormItem> getTrains() {
        return trains;
    }

    public boolean isInProgress() {
        return isInProgress;
    }
}
