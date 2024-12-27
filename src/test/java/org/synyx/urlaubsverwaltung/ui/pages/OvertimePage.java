package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.time.LocalDate;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static java.time.format.DateTimeFormatter.ofPattern;

public class OvertimePage {

    private static final String DUET_START_DATE_SELECTOR = "duet-date-picker [data-test-id=overtime-start-date]";
    private static final String DUET_END_DATE_SELECTOR = "duet-date-picker [data-test-id=overtime-end-date]";
    private static final String HOURS_SELECTOR = "[data-test-id=overtime-hours]";
    private static final String MINUTES_SELECTOR = "[data-test-id=overtime-minutes]";
    private static final String SUBMIT_SELECTOR = "[data-test-id=overtime-submit-button]";

    private final Page page;

    public OvertimePage(Page page) {
        this.page = page;
    }

    public void startDate(LocalDate startDate) {
        final String dateString = ofPattern("d.M.yyyy").format(startDate);
        page.locator(DUET_START_DATE_SELECTOR).fill(dateString);
    }

    public void hours(int hours) {
        page.locator(HOURS_SELECTOR).fill(String.valueOf(hours));
    }

    public void minutes(int minutes) {
        page.locator(MINUTES_SELECTOR).fill(String.valueOf(minutes));
    }

    public void submit() {
        page.waitForResponse(Response::ok, () -> page.locator(SUBMIT_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public boolean showsEndDate(LocalDate endDate) {
        final String value = page.locator(DUET_END_DATE_SELECTOR).inputValue();
        final String expectedDateString = ofPattern("d.M.yyyy").format(endDate);
        return value.equals(expectedDateString);
    }
}
