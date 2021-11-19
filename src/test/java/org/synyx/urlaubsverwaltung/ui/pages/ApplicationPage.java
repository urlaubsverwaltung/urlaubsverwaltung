package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ofPattern;

public class ApplicationPage implements Page {

    private static final By FROM_INPUT_SELECTOR = By.id("from");
    private static final By SUBMIT_SELECTOR = By.cssSelector("button#apply-application");
    private static final By VACATION_TYPE_SELECT_SELECTOR = By.cssSelector("[data-test-id=vacation-type-select]");

    private final WebDriver driver;

    public ApplicationPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return vacationTypeSelectExists(driver) && fromInputExists(driver);
    }

    public void from(LocalDate date) {
        final String dateString = ofPattern("dd.MM.yyyy").format(date);
        driver.findElement(FROM_INPUT_SELECTOR).sendKeys(dateString);
    }

    /**
     * selected the given person in the  replacement select box.
     * Note that this does not submit the form! Maybe there is JavaScript loaded which does it, though.
     *
     * @param person person that should be selected
     */
    public void selectReplacement(Person person) {
        final WebElement selectElement = driver.findElement(By.cssSelector("[data-test-id=holiday-replacement-select]"));
        final Select select = new Select(selectElement);
        select.selectByValue(String.valueOf(person.getId()));
    }

    public void setCommentForReplacement(Person person, String comment) {
        final HolidayReplacementRowElement holidayReplacementRow = getHolidayReplacementRow(person);

        if (holidayReplacementRow == null) {
            throw new IllegalStateException("could not find replacement row for the given person.");
        }

        final WebElement textarea = holidayReplacementRow.rowElement.findElement(By.tagName("textarea"));
        textarea.clear();
        textarea.sendKeys(comment);

        // for whatever reasons we have to blur the textarea afterwards
        // otherwise a single form submit click doesn't submit the form...
        final JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("Boolean(document.activeElement) ? document.activeElement.blur() : 0");
    }

    /**
     * Checks if the given person is visible at the given position of added replacements.
     *
     * @param person person that should be visible
     * @param position the position to check against. starts with 1.
     * @return <code>true</code> if the person is visible at the given position, <code>false</code> otherwise.
     */
    public boolean showsAddedReplacementAtPosition(Person person, int position) {
        return this.showsAddedReplacementAtPosition(person, position, "");
    }

    /**
     * Checks if the given person is visible at the given position of added replacements and if it has the given comment.
     *
     * @param person person that should be visible
     * @param position the position to check against. starts with 1.
     * @param comment the comment for the replacement
     * @return <code>true</code> if the person is visible at the given position, <code>false</code> otherwise.
     */
    public boolean showsAddedReplacementAtPosition(Person person, int position, String comment) {
        if (position < 1) {
            throw new IllegalArgumentException("position must be greater 0.");
        }

        final HolidayReplacementRowElement holidayReplacementRow = getHolidayReplacementRow(person);
        if (holidayReplacementRow == null) {
            return false;
        }

        final WebElement row = holidayReplacementRow.rowElement;
        final int rowPosition = holidayReplacementRow.position;

        if (position != rowPosition) {
            return false;
        }

        final WebElement textarea = row.findElement(By.tagName("textarea"));
        return textarea.getAttribute("value").equals(comment);
    }

    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    private static boolean fromInputExists(WebDriver driver) {
        return !driver.findElements(FROM_INPUT_SELECTOR).isEmpty();
    }

    private static boolean vacationTypeSelectExists(WebDriver driver) {
        return !driver.findElements(VACATION_TYPE_SELECT_SELECTOR).isEmpty();
    }

    private HolidayReplacementRowElement getHolidayReplacementRow(Person person) {
        final List<WebElement> rows = driver.findElements(By.cssSelector("[data-test-id=holiday-replacement-row]"));

        for (int i = 0; i < rows.size(); i++) {
            final WebElement row = rows.get(i);
            final List<WebElement> hiddenInputElements = row.findElements(By.cssSelector("input[type=hidden]"));

            final boolean isRowOfPerson = hiddenInputElements.stream().anyMatch(input -> {
                final String name = input.getAttribute("name");
                final String value = input.getAttribute("value");
                return name.startsWith("holidayReplacements[")
                    && name.endsWith("].person")
                    && value.equals(String.valueOf(person.getId()));
            });

            if (isRowOfPerson) {
                return new HolidayReplacementRowElement(row, i + 1);
            }
        }

        return null;
    }

    private static class HolidayReplacementRowElement {
        final WebElement rowElement;
        final int position;

        HolidayReplacementRowElement(WebElement rowElement, int position) {
            this.rowElement = rowElement;
            this.position = position;
        }
    }
}
