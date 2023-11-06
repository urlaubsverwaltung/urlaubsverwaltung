package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import java.util.Locale;
import java.util.regex.Pattern;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class SettingsPage {

    private static final String WORKING_TIME_TAB_SELECTOR = "[data-test-id=settings-tab-working-time]";
    private static final String ABSENCE_TYPES_TAB_SELECTOR = "[data-test-id=settings-tab-absence-types]";
    private static final String OVERTIME_ENABLED_SELECTOR = "[data-test-id=setting-overtime-enabled]";
    private static final String OVERTIME_DISABLED_SELECTOR = "[data-test-id=setting-overtime-disabled]";
    private static final String SAVE_BUTTON_SELECTOR = "[data-test-id=settings-save-button]";
    private static final String HALF_DAY_DISABLE_SELECTOR = "[data-test-id=vacation-half-day-disable]";

    private final Page page;

    public SettingsPage(Page page) {
        this.page = page;
    }

    public void clickWorkingTimeTab() {
        page.waitForResponse(Response::ok, () -> page.locator(WORKING_TIME_TAB_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public void clickAbsenceTypesTab() {
        page.locator(ABSENCE_TYPES_TAB_SELECTOR).click();
        page.waitForURL(Pattern.compile("/settings/absence-types$"));
    }

    public void enableOvertime() {
        page.locator(OVERTIME_ENABLED_SELECTOR).click();
    }

    public void disableOvertime() {
        page.locator(OVERTIME_DISABLED_SELECTOR).click();
    }

    public void addNewVacationType() {
        page.locator("button[name=add-absence-type]").click();
    }

    public Locator lastVacationType() {
        return page.locator("[data-test-id=vacation-type]").last();
    }

    public void setVacationTypeLabel(Locator absenceTypeLocator, Locale locale, String value) {
        final String selector = "[data-test-id=vacation-type-label-translation-%s]".formatted(locale.toString());
        absenceTypeLocator.locator(selector).fill(value);
    }

    public Locator vacationTypeMissingTranslationError(Locator absencetypeLocator) {
        return absencetypeLocator.locator("[data-test-id=vacation-type-missing-translation-error]");
    }

    public Locator vacationTypeUniqueTranslationError(Locator absencetypeLocator, Locale locale) {
        return absencetypeLocator.locator("[data-test-id=vacation-type-unique-translation-error-%s]".formatted(locale));
    }

    public Locator vacationTypeStatusCheckbox(Locator absenceTypeLocator) {
        return absenceTypeLocator.locator("[data-test-id=vacation-type-active]");
    }

    /**
     * Submits the setting form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void saveSettings() {
        page.waitForResponse(Response::ok, () -> page.locator(SAVE_BUTTON_SELECTOR).first().click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public void clickDisableHalfDayAbsence() {
        page.locator(HALF_DAY_DISABLE_SELECTOR).click();
    }
}
