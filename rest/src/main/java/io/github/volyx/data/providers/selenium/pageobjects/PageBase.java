package io.github.volyx.data.providers.selenium.pageobjects;

import io.github.volyx.data.SearchException;
import org.openqa.selenium.WebDriver;


public abstract class PageBase {
    protected final WebDriver driver;

    protected PageBase(WebDriver driver) {
        this.driver = driver;
    }

    protected void wait(int timeout) throws SearchException {
        synchronized (driver) {
            try {
                driver.wait(timeout);
            } catch (InterruptedException e) {
                throw new SearchException("wait failed", e);
            }
        }
    }
}
