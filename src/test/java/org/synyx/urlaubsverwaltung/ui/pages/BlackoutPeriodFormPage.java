package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.AriaRole.BUTTON;
import static com.microsoft.playwright.options.AriaRole.CHECKBOX;
import static com.microsoft.playwright.options.AriaRole.RADIO;

public class BlackoutPeriodFormPage {

    private final Page page;

    public BlackoutPeriodFormPage(Page page) {
        this.page = page;
    }

    public void open(int port) {
        page.navigate("http://localhost:%d/web/blackoutperiod/new".formatted(port));
    }

    public void chooseSelectedDepartments() {
        page.getByRole(RADIO, new Page.GetByRoleOptions().setName("Ausgewählte Abteilungen")).check();
    }

    public void chooseSelectedVacationTypes() {
        page.getByRole(RADIO, new Page.GetByRoleOptions().setName("Ausgewählte Abwesenheitsarten")).check();
    }

    public Locator departmentSelection() {
        return page.locator("[data-blackout-departments]");
    }

    public void searchDepartment(String query) {
        departmentSelection().locator("[data-blackout-department-search]").fill(query);
    }

    public void checkDepartment(String name) {
        departmentSelection().getByRole(CHECKBOX, new Locator.GetByRoleOptions().setName(name).setExact(false)).check();
    }

    public Locator departmentChips() {
        return departmentSelection().locator("[data-blackout-department-chip]");
    }

    public void removeDepartmentChip(String name) {
        departmentSelection().getByRole(BUTTON, new Locator.GetByRoleOptions().setName(name + " entfernen")).click();
    }

    public void checkVacationType(String label) {
        page.locator("input[name='vacationTypeIds'][data-label='%s']".formatted(label)).check();
    }

    public void startDate(String date) {
        fillDate("#startDate", date);
    }

    public void endDate(String date) {
        fillDate("#endDate", date);
    }

    private void fillDate(String inputSelector, String date) {
        final Locator input = page.locator("duet-date-picker " + inputSelector);
        input.fill(date);
        input.blur();
    }

    public Locator conflicts() {
        return page.locator("#blackout-period-conflicts");
    }

    public Locator impactSentence() {
        return page.locator("[data-blackout-impact-sentence]");
    }

    public Locator affectedPersons() {
        return page.locator("[data-blackout-person]").visible();
    }

    public Locator affectedPersonsHeading() {
        return page.locator("[data-blackout-impact-persons-heading]");
    }
}
