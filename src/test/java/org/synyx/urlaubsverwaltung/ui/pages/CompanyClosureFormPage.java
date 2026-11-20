package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

public class CompanyClosureFormPage {

    private final Page page;

    public CompanyClosureFormPage(Page page) {
        this.page = page;
    }

    public static String newUrl(int port) {
        return "http://localhost:%d/web/company-closure/new".formatted(port);
    }

    /**
     * @param monthDay month and day of the closure day as the url has it, e.g. "06-16"
     */
    public static String editUrl(int port, String monthDay) {
        return "http://localhost:%d/web/company-closure/%s/edit".formatted(port, monthDay);
    }

    public void open(int port) {
        page.navigate(newUrl(port));
    }

    public void day(String day) {
        page.locator("#day").fill(day);
    }

    public void month(String month) {
        page.locator("#month").fill(month);
    }

    public void germanName(String name) {
        page.locator("[data-test-id=company-closure-name-de]").fill(name);
    }

    public void save() {
        page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Speichern")).click();
    }

    public void cancel() {
        page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Abbrechen")).click();
    }

    public Locator dayError() {
        return page.locator("#day-error");
    }

    public Locator monthError() {
        return page.locator("#month-error");
    }

    public Locator namesError() {
        return page.locator("#names-error");
    }

    public Locator monthDayError() {
        return page.locator("#monthDay-error");
    }

    /**
     * @return the datepickers of "valid from" that are ready to use
     */
    public Locator validFromDatepicker() {
        return page.locator("duet-date-picker.hydrated[name=validFrom]");
    }

    /**
     * @return the fields submitted as "valid from", more than one would submit a stale date
     */
    public Locator validFromFields() {
        return page.locator("input[name=validFrom]");
    }

    /**
     * Marks the current document. The mark is gone as soon as the browser loads a page instead of rendering
     * the response into the current one.
     */
    public void markDocument() {
        page.evaluate("() => { document.documentElement.sameDocument = true; }");
    }

    public Locator markedDocument() {
        return page.locator("html");
    }
}
