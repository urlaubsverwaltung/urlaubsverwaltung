package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

public class ApplicationPage implements Page {

    private static final By FROM_INPUT_SELECTOR = By.id("from");
    private static final By SUBMIT_SELECTOR = By.cssSelector("button#apply-application");

    private final WebDriver driver;
    private final MessageSource messageSource;
    private final Locale locale;

    public ApplicationPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return title(driver) && fromInputExists(driver);
    }

    public void from(LocalDate date) {
        final String dateString = ofPattern("dd.MM.yyyy").format(date);
        driver.findElement(FROM_INPUT_SELECTOR).sendKeys(dateString);
    }

    /**
     * selected the given person in the  holiday replacement select box.
     * Note that this does not submit the form! Maybe there is JavaScript loaded which does it, though.
     *
     * @param person person that should be selected
     */
    public void selectReplacement(Person person) {
        final WebElement selectElement = driver.findElement(By.cssSelector("[data-test-id=holiday-replacement-select]"));
        final Select select = new Select(selectElement);
        select.selectByValue(String.valueOf(person.getId()));
    }

    /**
     * Checks if the given person is visible at the given position of added holiday replacements.
     *
     * @param person person that should be visible
     * @param position the position to check against. starts with 1.
     * @return <code>true</code> if the person is visible at the given position, <code>false</code> otherwise.
     */
    public boolean showsAddedReplacementAtPosition(Person person, int position) {
        if (position < 1) {
            throw new IllegalArgumentException("position must be greater 0.");
        }

        final List<WebElement> rows = driver.findElements(By.cssSelector("[data-test-id=holiday-replacement-row]"));
        if (rows.size() < position) {
            return false;
        }

        final WebElement row = rows.get(position - 1);

        final List<WebElement> hiddenInputElements = row.findElements(By.cssSelector("input[type=hidden]"));

        return hiddenInputElements.stream().anyMatch(input -> {
            final String name = input.getAttribute("name");
            final String value = input.getAttribute("value");
            return name.startsWith("holidayReplacements[")
                && name.endsWith("].person")
                && value.equals(String.valueOf(person.getId()));
        });
    }

    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    private boolean title(WebDriver driver) {
        final String titleText = messageSource.getMessage("application.data.header.title.new", new Object[]{}, locale);
        return driver.getTitle().equals(titleText);
    }

    private static boolean fromInputExists(WebDriver driver) {
        return !driver.findElements(FROM_INPUT_SELECTOR).isEmpty();
    }
}
