package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.util.Locale;

public class ApplicationDetailPage implements Page {

    private static final By ALERT_SUCCESS_SELECTOR = By.className("alert-success");

    private final WebDriver driver;
    private final MessageSource messageSource;
    private final Locale locale;

    public ApplicationDetailPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisibleForPerson(String username) {
        return driver.getTitle().equals(title(username));
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return driver.getTitle().startsWith(title(""));
    }

    public boolean showsApplicationCreatedInfo() {
        final WebElement element = driver.findElement(ALERT_SUCCESS_SELECTOR);
        return element != null && element.isDisplayed();
    }

    private String title(String username) {
        return messageSource.getMessage("application.data.header.title", new Object[]{username}, locale);
    }
}
