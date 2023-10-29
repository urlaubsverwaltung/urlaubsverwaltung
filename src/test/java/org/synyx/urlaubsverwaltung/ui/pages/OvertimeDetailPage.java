package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class OvertimeDetailPage {

    private static final String ALERT_SUCCESS_SELECTOR = ".alert-success";
    private static final String PERSON_SELECTOR = "[data-test-id=overtime-person]";
    private static final String DURATION_SELECTOR = "[data-test-id=overtime-duration]";

    private final Page page;

    public OvertimeDetailPage(Page page) {
        this.page = page;
    }

    public boolean isVisibleForPerson(String username) {
        return page.locator(PERSON_SELECTOR).textContent().strip().startsWith(username);
    }

    public boolean showsOvertimeCreatedInfo() {
        final Locator element = page.locator(ALERT_SUCCESS_SELECTOR);
        return element != null && element.isVisible();
    }

    public boolean showsHours(int hours) {
        final String durationText = page.locator(DURATION_SELECTOR).textContent().strip();
        // this test may be a false positive when the expected hours are the same as the actual minutes of the overtime entry
        // while the overtime entry contains minutes only... improve it when you need it.
        return durationText.startsWith(hours + " ");
    }

    public boolean showsMinutes(int minutes) {
        final String durationText = page.locator(DURATION_SELECTOR).textContent().strip();
        // check if the second digit chunk matches the expected minutes
        // this test fails when the overtime entry contains minutes only, of course. improve it when you need it.
        return durationText.matches(String.format("^\\d+ \\w+\\. %d \\w+\\.", minutes));
    }
}
