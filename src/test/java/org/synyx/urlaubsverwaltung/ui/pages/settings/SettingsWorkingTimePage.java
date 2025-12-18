package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;

public class SettingsWorkingTimePage {

    private static final String OVERTIME_ENABLED_SELECTOR = "[data-test-id=setting-overtime-enabled]";
    private static final String OVERTIME_DISABLED_SELECTOR = "[data-test-id=setting-overtime-disabled]";

    private final Page page;

    public SettingsWorkingTimePage(Page page) {
        this.page = page;
    }

    public void enableOvertime() {
        page.locator(OVERTIME_ENABLED_SELECTOR).click();
    }

    public void disableOvertime() {
        page.locator(OVERTIME_DISABLED_SELECTOR).click();
    }

    /**
     * Submits the overtime form and waits for dom-content loaded.
     */
    public void submitOvertimeForm() {
        page.locator("[data-test-id=submit-overtime-button]").click();
    }
}
