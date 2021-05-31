package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SickNoteOverviewPage implements Page {

    private static final By TABLE_SELECTOR = By.cssSelector("[data-test-id=sick-notes-table]");
    private final WebDriver driver;

    public SickNoteOverviewPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return driver.getTitle().equals("Sick notes");
    }

    public boolean showsSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        Predicate<String> sickDaysText = textContent -> textContent.contains(sickDays + " Sick days");
        Predicate<String> hasSickDaysCertificate = textContent -> textContent.contains(daysWithIncapacityCertificate + " days with certificate of incapacity for work");

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(WebElement::getText)
            .filter(sickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    public boolean showsChildSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        Predicate<String> sickDaysText = textContent -> textContent.contains(sickDays + " Child sick days");
        Predicate<String> hasSickDaysCertificate = textContent -> textContent.contains(daysWithIncapacityCertificate + " days with certificate of incapacity for work");

        if (daysWithIncapacityCertificate == 0) {
            hasSickDaysCertificate = Predicate.not(hasSickDaysCertificate);
        }

        return rowWithPerson(firstName, lastName)
            .map(WebElement::getText)
            .filter(sickDaysText.and(hasSickDaysCertificate))
            .isPresent();
    }

    private Optional<WebElement> rowWithPerson(String firstName, String lastName) {

        final WebElement table = driver.findElement(TABLE_SELECTOR);
        final List<WebElement> tableRows = table.findElements(By.cssSelector("tr"));

        for (WebElement tableRow : tableRows) {
            if (tableRow.getText().contains(firstName + " " + lastName)) {
                return Optional.of(tableRow);
            }
        }

        return Optional.empty();
    }
}
