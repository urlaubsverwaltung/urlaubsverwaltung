package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;
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

    public boolean showsReplacement(Person person) {
        final WebElement holidayReplacementList = driver.findElement(By.cssSelector("[data-test-id=holiday-replacement-list]"));
        return holidayReplacementList.getText().contains(person.getNiceName());
    }

    public void selectEdit() {
        final WebElement button = driver.findElement(By.cssSelector("[data-test-id=application-edit-button]"));
        button.click();
    }

    private String title(String username) {
        return messageSource.getMessage("application.data.header.title", new Object[]{username}, locale);
    }
}
