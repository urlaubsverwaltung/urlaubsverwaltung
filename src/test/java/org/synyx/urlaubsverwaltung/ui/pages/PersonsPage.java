package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PersonsPage {

    private final Page page;
    private final PaginationPage pagination;

    public PersonsPage(Page page) {
        this.page = page;
        this.pagination = new PaginationPage("persons", page);
    }

    /**
     * Returns the person row locator for the given criteria.
     *
     * @param rowZeroBased   row number
     * @param personNiceName name of the person
     * @return matching Locator
     */
    public Locator getPersonRowLocator(int rowZeroBased, String personNiceName) {
        // TODO use :nth child or something instead of unique testId (persons-row:nth-child(x) instead of persons-row-x)
        return page.locator("[data-test-id=persons-row-%s]".formatted(rowZeroBased))
            .filter(new Locator.FilterOptions().setHasText(personNiceName));
    }

    /**
     * Returns the button opening the dropdown to select the shown person group
     * (active persons, a department or inactive persons).
     *
     * @return matching Locator
     */
    public Locator getPersonGroupButtonLocator() {
        return page.locator("#person-group-popover-button");
    }

    /**
     * Returns the link of the given person group within the (opened) person group dropdown.
     *
     * @param personGroupName name of the person group, e.g. the department name
     * @return matching Locator
     */
    public Locator getPersonGroupLocator(String personGroupName) {
        return page.locator("#person-group-selection-popover")
            .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(personGroupName));
    }

    public void openPersonGroupDropdown() {
        getPersonGroupButtonLocator().click();
        assertThat(page.locator("#person-group-selection-popover")).isVisible();
    }

    public void selectPersonGroup(String personGroupName) {
        getPersonGroupLocator(personGroupName).click();
    }

    public void showsPersonGroup(String personGroupName) {
        assertThat(getPersonGroupButtonLocator()).hasText(personGroupName);
    }

    public PaginationPage getPersonsPagination() {
        return pagination;
    }

    public void showsNthPersons(int numberOfPersons) {
        // TODO instead of <tr> use data-test-id locator?
        assertThat(page.locator("[data-test-id=persons]").locator("tbody").locator("tr"))
            .hasCount(numberOfPersons);
    }

    public void showsPersonRow(int rowZeroBased, String personNiceName) {
        assertThat(getPersonRowLocator(rowZeroBased, personNiceName)).isVisible();
    }
}
