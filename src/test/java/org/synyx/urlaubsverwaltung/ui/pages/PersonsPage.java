package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

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
     * @param rowZeroBased row number
     * @param personNiceName name of the person
     * @return matching Locator
     */
    public Locator getPersonRowLocator(int rowZeroBased, String personNiceName) {
        // TODO use :nth child or something instead of unique testId (persons-row:nth-child(x) instead of persons-row-x)
        return page.locator("[data-test-id=persons-row-%s]".formatted(rowZeroBased))
            .filter(new Locator.FilterOptions().setHasText(personNiceName));
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
