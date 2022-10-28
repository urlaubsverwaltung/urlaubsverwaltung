package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class SickNoteOverviewPage implements Page {

    private static final By TABLE_SELECTOR = By.cssSelector("[data-test-id=sick-notes-table]");

    private final WebDriver driver;
    private final Locale locale;
    private final MessageSource messageSource;

    public SickNoteOverviewPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.locale = locale;
        this.messageSource = messageSource;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        final String sickNoteHeaderTitle = messageSource.getMessage("sicknotes.header.title", new Object[]{}, locale);
        return driver.getTitle().equals(sickNoteHeaderTitle);
    }

    public boolean showsSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = messageSource.getMessage("sicknotes.daysOverview.sickDays.number", new Object[]{}, locale);
        final String sickDaysAubText = messageSource.getMessage("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate}, locale);

        Predicate<String> hasSickDaysText = textContent -> textContent.contains(sickDays + " " + sickDaysText);
        Predicate<String> hasSickDaysCertificate = textContent -> textContent.contains(sickDaysAubText);

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(WebElement::getText)
            .filter(hasSickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    public boolean showsChildSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = messageSource.getMessage("sicknotes.daysOverview.sickDays.child.number", new Object[]{}, locale);
        final String sickDaysAubText = messageSource.getMessage("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate}, locale);

        Predicate<String> hasSickDaysText = textContent -> textContent.contains(sickDays + " " + sickDaysText);
        Predicate<String> hasSickDaysCertificate = textContent -> textContent.contains(sickDaysAubText);

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(WebElement::getText)
            .filter(hasSickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    private Optional<WebElement> rowWithPerson(String firstName, String lastName) {

        final WebElement table = driver.findElement(TABLE_SELECTOR);
        final List<WebElement> tableRows = table.findElements(By.cssSelector("tr"));

        for (WebElement tableRow : tableRows) {
            if (tableRow.getText().contains(firstName) && tableRow.getText().contains(lastName)) {
                return Optional.of(tableRow);
            }
        }

        return Optional.empty();
    }
}
