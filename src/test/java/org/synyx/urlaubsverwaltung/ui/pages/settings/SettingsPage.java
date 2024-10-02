package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class SettingsPage {

    private static final String SAVE_BUTTON_SELECTOR = "[data-test-id=settings-save-button]";
    private static final String HALF_DAY_DISABLE_SELECTOR = "[data-test-id=vacation-half-day-disable]";

    private final Page page;

    public SettingsPage(Page page) {
        this.page = page;
    }

    public SettingsSubNavigation navigation() {
        return new SettingsSubNavigation(page);
    }

    /**
     * Submits the setting form and waits for dom-content loaded.
     */
    public void saveSettings() {
        page.waitForResponse(Response::ok, () -> page.locator(SAVE_BUTTON_SELECTOR).first().click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public void clickDisableHalfDayAbsence() {
        page.locator(HALF_DAY_DISABLE_SELECTOR).click();
    }

    public void clickUserSubmitSickNotesAllowed() {
        page.locator("[data-test-id=user-allowed-to-submit-sicknote-input][value=true]").click();
    }

    public void clickUserToSubmitSickNotesForbidden() {
        page.locator("[data-test-id=user-allowed-to-submit-sicknote-input][value=false]").click();
    }
}
