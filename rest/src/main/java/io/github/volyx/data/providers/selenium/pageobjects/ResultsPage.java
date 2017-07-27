package io.github.volyx.data.providers.selenium.pageobjects;

import io.github.volyx.data.SearchException;
import io.github.volyx.data.providers.selenium.pageobjects.results.ParseResult;
import io.github.volyx.data.providers.selenium.pageobjects.results.Parser;
import io.github.volyx.data.providers.selenium.pageobjects.results.ResultsFormItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;

import java.util.List;


public class ResultsPage extends PageBase {
    private final Log log = LogFactory.getLog(ResultsPage.class);

    public ResultsPage(WebDriver driver) {
        super(driver);
    }

    public List<ResultsFormItem> parse() throws SearchException {
        log.debug("parsing page");
        int nAttempts = 20;
        ParseResult result = null;
        while (nAttempts > 0) {
            nAttempts--;
            result = new Parser().parse(driver.getPageSource());
            if (!result.isInProgress()) {
                break;
            }
            log.debug("Result page is not loaded yet. Waiting...");
            wait(1000);
        }

        if (nAttempts <= 0) {
            log.debug("Number of attempts has been exceeded.");
            throw new SearchException("Number of attempts has been exceeded.");
        }
        log.debug("Result page has been loaded.");
        return result.getTrains();
    }
}
