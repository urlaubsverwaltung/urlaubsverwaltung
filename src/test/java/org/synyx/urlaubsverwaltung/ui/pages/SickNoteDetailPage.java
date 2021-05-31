package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNoteDetailPage implements Page {

    private static final By PERSON_SELECTOR = By.cssSelector("[data-test-id=sicknote-person]");
    private static final By TYPE_SELECTOR = By.cssSelector("[data-test-id=sicknote-type]");
    private static final By DATE_SELECTOR = By.cssSelector("[data-test-id=sicknote-date]");
    private static final By AUB_DATE_SELECTOR = By.cssSelector("[data-test-id=sicknote-aub-date]");

    private final WebDriver driver;
    private final MessageSource messageSource;
    private final Locale locale;

    public SickNoteDetailPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(PERSON_SELECTOR).isEmpty() && !driver.findElements(DATE_SELECTOR).isEmpty();
    }

    public boolean showsSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknote", new Object[]{}, locale);

        return driver.findElement(PERSON_SELECTOR).getText().contains(name)
            && driver.findElement(TYPE_SELECTOR).getText().contains(typeText);
    }

    public boolean showsChildSickNoteForPerson(String name) {
        final String typeText = messageSource.getMessage("application.data.sicknotetype.sicknotechild", new Object[]{}, locale);
        return driver.findElement(PERSON_SELECTOR).getText().contains(name)
            && driver.findElement(TYPE_SELECTOR).getText().contains(typeText);
    }

    public boolean showsSickNoteDateFrom(LocalDate dateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateFrom);
        return driver.findElement(DATE_SELECTOR).getText().contains(expectedDateString);
    }

    public boolean showsSickNoteDateTo(LocalDate dateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(dateTo);
        return driver.findElement(DATE_SELECTOR).getText().contains(expectedDateString);
    }

    public boolean showsSickNoteAubDateFrom(LocalDate aubDateFrom) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateFrom);
        return driver.findElement(AUB_DATE_SELECTOR).getText().contains(expectedDateString);
    }

    public boolean showsSickNoteAubDateTo(LocalDate aubDateTo) {
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(aubDateTo);
        return driver.findElement(AUB_DATE_SELECTOR).getText().contains(expectedDateString);
    }

    public boolean showsNoIncapacityCertificate() {
        final String notPresentText = messageSource.getMessage("sicknote.data.aub.notPresent", new Object[]{}, locale);
        return driver.findElement(AUB_DATE_SELECTOR).getText().contains(notPresentText);
    }
}
