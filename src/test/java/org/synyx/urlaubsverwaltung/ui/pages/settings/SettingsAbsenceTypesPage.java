package org.synyx.urlaubsverwaltung.ui.pages.settings;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Locale;

import static com.microsoft.playwright.options.WaitForSelectorState.ATTACHED;

public class SettingsAbsenceTypesPage {

    private final Page page;

    public SettingsAbsenceTypesPage(Page page) {
        this.page = page;
    }

    public void addNewVacationType() {
        page.locator("button[name=add-absence-type]").click();
        page.locator("[data-test-id=vacation-type] .absence-type-card__label span:empty").waitFor(new Locator.WaitForOptions().setState(ATTACHED));
    }

    /**
     * Waits until the absence type list has been rendered. {@link Locator#count()} does not wait by itself,
     * so counting rows right after the navigation would count an empty page.
     */
    public void waitForVisible() {
        page.locator("[data-test-id=vacation-type]").first().waitFor();
    }

    public Locator vacationTypes() {
        return page.locator("[data-test-id=vacation-type]");
    }

    public Locator lastVacationType() {
        return page.locator("[data-test-id=vacation-type]").last();
    }

    public void removeVacationType(Locator absenceTypeLocator) {
        absenceTypeLocator.locator("[data-test-id=remove-absence-type-button]").click();
    }

    public Locator saveSuccessFeedback() {
        return page.locator("[data-test-id=feedback-box].alert-success");
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
     * Submits the form, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void submitCustomAbsenceTypes() {
        page.locator("[data-test-id=settings-save-button]").click();
    }
}
