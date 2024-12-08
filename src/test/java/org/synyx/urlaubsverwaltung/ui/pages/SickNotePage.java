package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.time.LocalDate;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static java.time.format.DateTimeFormatter.ofPattern;

public class SickNotePage {

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

    public SickNotePage(Page page) {
        this.page = page;
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
     * Submits the sick note form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void submit() {
        page.waitForResponse(Response::ok, () -> page.locator(SUBMIT_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public boolean dayTypeFullSelected() {
        return page.locator(DAY_TYPE_FULL_SELECTOR).isChecked();
    }

    public boolean typeSickNoteSelected() {
        return page.locator(SICKNOTE_TYPE_SELECTOR).inputValue().equals(Type.SICK_NOTE.value);
    }

    public boolean showsToDate(LocalDate fromDate) {
        final String expectedDateString = ofPattern("d.M.yyyy").format(fromDate);
        final String value = page.locator(TO_SELECTOR).inputValue();
        return value.equals(expectedDateString);
    }

    public boolean showsAubToDate(LocalDate fromDate) {
        final String expectedDateString = ofPattern("d.M.yyyy").format(fromDate);
        final String value = page.locator(AUB_TO_SELECTOR).inputValue();
        return value.equals(expectedDateString);
    }

    /**
     * Check if the given name matches the selected person.
     *
     * @param name the displayed username
     * @return <code>true</code> when the given name is the selected element, <code>false</code> otherwise
     */
    public boolean personSelected(String name) {
        final Locator selectElement = page.locator(PERSON_SELECTOR);
        final String value = (String) selectElement.evaluate("node => node.value");
        final Locator optionElement = selectElement.locator("option[value=\"" + value + "\"]");
        return optionElement.textContent().strip().equals(name);
    }

    private void setDate(LocalDate date, String selector) {
        final String dateString = ofPattern("d.M.yyyy").format(date);
        final Locator input = page.locator(selector);
        input.fill("");
        input.fill(dateString);
    }
}
