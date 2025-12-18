package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Page;

public class SettingsAbsencesPage {

    private static final String DATA_PAGE = "main[data-page='settings-absences']";

    private static final String SAVE_BUTTON_SELECTOR = "[data-test-id=settings-save-button]";
    private static final String HALF_DAY_DISABLE_SELECTOR = "[data-test-id=vacation-half-day-disable]";

    private final Page page;

    public SettingsAbsencesPage(Page page) {
        this.page = page;
    }

    public void isVisible() {
        page.waitForSelector(DATA_PAGE);
    }

    public SettingsSubNavigation navigation() {
        return new SettingsSubNavigation(page);
    }

    /**
     * Submits the setting form and waits for dom-content loaded.
     */
    public void saveSettings() {
        page.locator(SAVE_BUTTON_SELECTOR).first().click();
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
