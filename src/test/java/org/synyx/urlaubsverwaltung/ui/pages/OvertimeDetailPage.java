package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class OvertimeDetailPage {

    private static final String ALERT_SUCCESS_SELECTOR = ".alert-success";
    private static final String PERSON_SELECTOR = "[data-test-id=overtime-person]";
    private static final String DURATION_SELECTOR = "[data-test-id=overtime-duration]";

    private final Page page;

    public OvertimeDetailPage(Page page) {
        this.page = page;
    }

    public void isVisibleForPerson(String username) {
        assertThat(page.locator(PERSON_SELECTOR)).containsText(username);
    }

    public void showsOvertimeCreatedInfo() {
        assertThat(page.locator(ALERT_SUCCESS_SELECTOR)).isVisible();
    }

    public void showsHours(int hours) {
        // this test may be a false positive when the expected hours are the same as the actual minutes of the overtime entry
        // while the overtime entry contains minutes only... improve it when you need it.
        final Pattern startsWithHours = Pattern.compile("^%d ".formatted(hours));
        assertThat(page.locator(DURATION_SELECTOR)).hasText(startsWithHours);
    }

    public void showsMinutes(int minutes) {
        // check if the second digit chunk matches the expected minutes
        // this test fails when the overtime entry contains minutes only, of course. improve it when you need it.
        final Pattern minutesPattern = Pattern.compile("^\\d+ \\w+\\. %d \\w+\\.".formatted(minutes));
        assertThat(page.locator(DURATION_SELECTOR)).hasText(minutesPattern);
    }
}
