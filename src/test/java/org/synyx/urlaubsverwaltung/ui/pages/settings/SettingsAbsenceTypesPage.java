package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Locale;

import static com.microsoft.playwright.options.WaitForSelectorState.ATTACHED;
import static org.synyx.urlaubsverwaltung.ui.pages.UvPage.executeAndWaitForPageRefresh;

public class SettingsAbsenceTypesPage {

    private final Page page;

    public SettingsAbsenceTypesPage(Page page) {
        this.page = page;
    }

    public void addNewVacationType() {
        page.locator("button[name=add-absence-type]").click();
        page.locator("[data-test-id=vacation-type] .absence-type-card__label span:empty").waitFor(new Locator.WaitForOptions().setState(ATTACHED));
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

    public void submitCustomAbsenceTypesAndWaitForPageRefresh() {
        executeAndWaitForPageRefresh(page, page ->
            page.locator("[data-test-id=submit-custom-absence-types-button]").click());
    }
}
