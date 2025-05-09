package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.regex.Pattern;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

/**
 * Represents the sub navigation of the {@link SettingsPage}
 */
public class SettingsSubNavigation {

    private static final String OVERTIME_TAB_SELECTOR = "[data-test-id=settings-tab-overtime]";
    private static final String ABSENCE_TYPES_TAB_SELECTOR = "[data-test-id=settings-tab-absence-types]";

    private final Page page;

    SettingsSubNavigation(Page page) {
        this.page = page;
    }

    public void goToOvertime() {
        page.waitForResponse(Response::ok, () -> page.locator(OVERTIME_TAB_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public void goToAbsenceTypes() {
        page.locator(ABSENCE_TYPES_TAB_SELECTOR).click();
        page.waitForURL(Pattern.compile("/settings/absence-types$"));
    }
}
