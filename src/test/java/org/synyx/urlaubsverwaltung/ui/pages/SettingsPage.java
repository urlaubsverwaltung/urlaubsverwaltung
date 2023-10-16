package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class SettingsPage {

    private static final String WORKING_TIME_TAB_SELECTOR = "[data-test-id=settings-tab-working-time]";
    private static final String WORKING_TIME_TAB_ACTIVE_SELECTOR = "[data-test-id=settings-tab-working-time].active";
    private static final String OVERTIME_ENABLED_SELECTOR = "[data-test-id=setting-overtime-enabled]";
    private static final String OVERTIME_DISABLED_SELECTOR = "[data-test-id=setting-overtime-disabled]";
    private static final String SAVE_BUTTON_SELECTOR = "[data-test-id=settings-save-button]";
    private static final String HALF_DAY_DISABLE_SELECTOR = "[data-test-id=vacation-half-day-disable]";

    private final Page page;

    public SettingsPage(Page page) {
        this.page = page;
    }

    public boolean isVisible() {
        return page.locator(WORKING_TIME_TAB_SELECTOR).isVisible();
    }

    public void clickWorkingTimeTab() {
        page.locator(WORKING_TIME_TAB_SELECTOR).click();
        page.waitForURL(Pattern.compile("/settings/working-time$"));
    }

    public void enableOvertime() {
        page.locator(OVERTIME_ENABLED_SELECTOR).click();
    }
    public void disableOvertime() {
        page.locator(OVERTIME_DISABLED_SELECTOR).click();
    }

    /**
     * Submits the setting form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void saveSettings() {
        page.locator(SAVE_BUTTON_SELECTOR).first().click();
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public void clickDisableHalfDayAbsence() {
        page.locator(HALF_DAY_DISABLE_SELECTOR).click();
    }
}
