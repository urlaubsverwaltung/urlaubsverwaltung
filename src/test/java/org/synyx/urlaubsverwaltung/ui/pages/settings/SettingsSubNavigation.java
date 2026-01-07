package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;

import static org.synyx.urlaubsverwaltung.ui.pages.UvPage.executeAndWaitForPageRefresh;

/**
 * Represents the sub navigation of the {@link SettingsAbsencesPage}
 */
public class SettingsSubNavigation {

    private static final String OVERTIME_TAB_SELECTOR = "[data-test-id=settings-tab-overtime]";
    private static final String ABSENCE_TYPES_TAB_SELECTOR = "[data-test-id=settings-tab-absence-types]";

    private final Page page;

    SettingsSubNavigation(Page page) {
        this.page = page;
    }

    public void goToOvertime() {
        executeAndWaitForPageRefresh(page, page ->
            page.locator(OVERTIME_TAB_SELECTOR).click());
    }

    public void goToAbsenceTypes() {
        executeAndWaitForPageRefresh(page, page ->
            page.locator(ABSENCE_TYPES_TAB_SELECTOR).click());
    }
}
