package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNotePage implements Page {

    private static final By PERSON_SELECTOR = By.cssSelector("[data-test-id=person-select]");
    private static final By SICKNOTE_TYPE_SELECTOR = By.cssSelector("[data-test-id=sicknote-type-select]");
    private static final By DAY_TYPE_FULL_SELECTOR = By.cssSelector("[data-test-id=day-type-full]");
    private static final By DAY_TYPE_MORNING_SELECTOR = By.cssSelector("[data-test-id=day-type-morning]");
    private static final By DAY_TYPE_NOON_SELECTOR = By.cssSelector("[data-test-id=day-type-noon]");
    private static final By FROM_SELECTOR = By.cssSelector("[data-test-id=sicknote-from-date]");
    private static final By TO_SELECTOR = By.cssSelector("[data-test-id=sicknote-to-date]");
    private static final By AUB_FROM_SELECTOR = By.cssSelector("[data-test-id=sicknote-aub-from]");
    private static final By AUB_TO_SELECTOR = By.cssSelector("[data-test-id=sicknote-aub-from]");
    private static final By SUBMIT_SELECTOR = By.cssSelector("[data-test-id=sicknote-submit-button]");

    private final WebDriver driver;

    private enum Type {
        SICK_NOTE("1000"),
        CHILD_SICK_NOTE("2000");

        final String value;

        Type(String value) {
            this.value = value;
        }
    }

    public SickNotePage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(SUBMIT_SELECTOR).isEmpty();
    }

    public void startDate(LocalDate startDate) {
        setDate(startDate, FROM_SELECTOR);
    }

    public void toDate(LocalDate toDate) {
        setDate(toDate, TO_SELECTOR);
    }

    public void selectTypeChildSickNote() {
        final Select select = new Select(driver.findElement(SICKNOTE_TYPE_SELECTOR));
        select.selectByValue(Type.CHILD_SICK_NOTE.value);
    }

    public void aubStartDate(LocalDate aubStartDate) {
        setDate(aubStartDate, AUB_FROM_SELECTOR);
    }

    /**
     * Submits the sick note form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    public boolean dayTypeFullSelected() {
        return driver.findElement(DAY_TYPE_FULL_SELECTOR).isSelected();
    }

    public boolean typeSickNoteSelected() {
        final String value = driver.findElement(SICKNOTE_TYPE_SELECTOR).getAttribute("value");
        return Type.SICK_NOTE.value.equals(value);
    }

    public boolean showsToDate(LocalDate fromDate) {
        final String value = driver.findElement(TO_SELECTOR).getAttribute("value");
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(fromDate);
        return value.equals(expectedDateString);
    }

    public boolean showsAubToDate(LocalDate fromDate) {
        final String value = driver.findElement(AUB_TO_SELECTOR).getAttribute("value");
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(fromDate);
        return value.equals(expectedDateString);
    }

    /**
     * Check if the given name matches the selected person.
     *
     * @param name the displayed username
     * @return <code>true</code> when the given name is the selected element, <code>false</code> otherwise
     */
    public boolean personSelected(String name) {
        final WebElement selectElement = driver.findElement(PERSON_SELECTOR);
        final String value = selectElement.getAttribute("value");
        final WebElement optionElement = selectElement.findElement(By.cssSelector("option[value=\"" + value + "\"]"));
        return optionElement.getText().strip().equals(name);
    }

    private void setDate(LocalDate date, By selector) {
        final String dateString = ofPattern("dd.MM.yyyy").format(date);
        final WebElement input = driver.findElement(selector);
        input.clear();
        input.sendKeys(dateString);
    }
}
