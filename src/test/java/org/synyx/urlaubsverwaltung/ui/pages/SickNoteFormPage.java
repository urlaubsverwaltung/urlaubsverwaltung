package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.time.LocalDate;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNoteFormPage {

    private static final String PERSON_SELECTOR = "[data-test-id=person-select]";
    private static final String SICKNOTE_TYPE_SELECTOR = "[data-test-id=sicknote-type-select]";
    private static final String DAY_TYPE_FULL_SELECTOR = "[data-test-id=day-type-full]";
    private static final String FROM_SELECTOR = "[data-test-id=sicknote-from-date]";
    private static final String TO_SELECTOR = "[data-test-id=sicknote-to-date]";
    private static final String AUB_FROM_SELECTOR = "[data-test-id=sicknote-aub-from]";
    private static final String AUB_TO_SELECTOR = "[data-test-id=sicknote-aub-from]";
    private static final String SUBMIT_SELECTOR = "[data-test-id=sicknote-submit-button]";

    private final Page page;

    private enum Type {
        SICK_NOTE("1953"),
        CHILD_SICK_NOTE("1954");

        final String value;

        Type(String value) {
            this.value = value;
        }
    }

    public SickNoteFormPage(Page page) {
        this.page = page;
    }

    public void waitForVisible() {
        page.waitForSelector(FROM_SELECTOR);
        page.waitForSelector(TO_SELECTOR);
    }

    public void startDate(LocalDate startDate) {
        setDate(startDate, FROM_SELECTOR);
    }

    public void toDate(LocalDate toDate) {
        setDate(toDate, TO_SELECTOR);
    }

    public void selectTypeChildSickNote() {
        page.selectOption(SICKNOTE_TYPE_SELECTOR, Type.CHILD_SICK_NOTE.value);
    }

    public void aubStartDate(LocalDate aubStartDate) {
        setDate(aubStartDate, AUB_FROM_SELECTOR);
    }

    /**
     * Submits the form, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void submit() {
        page.locator(SUBMIT_SELECTOR).click();
    }

    public void dayTypeFullSelected() {
        assertThat(page.locator(DAY_TYPE_FULL_SELECTOR)).isChecked();
    }

    public void typeSickNoteSelected() {
        assertThat(page.locator(SICKNOTE_TYPE_SELECTOR)).hasValue(Type.SICK_NOTE.value);
    }

    public void showsToDate(LocalDate fromDate) {
        final String expectedDateString = ofPattern("d.M.yyyy").format(fromDate);
        assertThat(page.locator(TO_SELECTOR)).hasValue(expectedDateString);
    }

    public void showsAubToDate(LocalDate fromDate) {
        final String expectedDateString = ofPattern("d.M.yyyy").format(fromDate);
        assertThat(page.locator(AUB_TO_SELECTOR)).hasValue(expectedDateString);
    }

    /**
     * Check if the given name matches the selected person.
     *
     * @param name the displayed username
     */
    public void personSelected(String name) {
        final Locator selectElement = page.locator(PERSON_SELECTOR);
        // read current selected value of <select> (which is the id of the person)
        final String value = (String) selectElement.evaluate("node => node.value");
        // and verify this selected person has the expected name
        assertThat(selectElement.locator("option[value=\"" + value + "\"]")).hasText(name);

        // note: there could be a race condition, I think?
        // evaluate reads the current value, which could not match the expected name,
        // because the value changes eventually to the matching name.
    }

    private void setDate(LocalDate date, String selector) {
        final String dateString = ofPattern("d.M.yyyy").format(date);
        final Locator input = page.locator(selector);
        input.fill("");
        input.fill(dateString);
    }
}
