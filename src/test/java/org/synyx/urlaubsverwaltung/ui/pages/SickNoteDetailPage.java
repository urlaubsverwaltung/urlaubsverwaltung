package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNoteDetailPage implements Page {

    private static final By PERSON_SELECTOR = By.cssSelector("[data-test-id=sicknote-person]");
    private static final By DATE_SELECTOR = By.cssSelector("[data-test-id=sicknote-date]");

    private final WebDriver driver;

    public SickNoteDetailPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(PERSON_SELECTOR).isEmpty() && !driver.findElements(DATE_SELECTOR).isEmpty();
    }

    public boolean showsSickNoteForPerson(String name) {
        return driver.findElement(PERSON_SELECTOR).getText().contains(name);
    }

    public boolean showsSickNoteDateFrom(LocalDate dateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateFrom);
        return driver.findElement(DATE_SELECTOR).getText().contains(expectedDateString);
    }
}
