package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;

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

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickOvertime() {
        page.locator(OVERTIME_TAB_SELECTOR).click();
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickAbsenceTypes() {
        page.locator(ABSENCE_TYPES_TAB_SELECTOR).click();
    }
}
