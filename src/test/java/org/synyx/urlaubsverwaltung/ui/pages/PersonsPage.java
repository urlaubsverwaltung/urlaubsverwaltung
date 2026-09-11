package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PersonsPage {

    private static final int MINIMUM_GAP_TO_SCREEN_EDGE = 8;

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

    /**
     * Asserts that the (opened) person group dropdown is within the viewport, horizontally, and keeps a
     * gap to both edges of the screen. Entries of a dropdown reaching over the edge of the screen cannot
     * be read or clicked, and one flush against it looks like it has been cut off.
     */
    public void showsPersonGroupDropdownWithinViewport() {

        final BoundingBox box = page.locator("#person-group-selection-popover").boundingBox();
        final int viewportWidth = page.viewportSize().width;

        org.assertj.core.api.Assertions.assertThat(box.x)
            .describedAs("dropdown must keep a gap to the left edge of the screen")
            .isGreaterThanOrEqualTo(MINIMUM_GAP_TO_SCREEN_EDGE);

        org.assertj.core.api.Assertions.assertThat(box.x + box.width)
            .describedAs("dropdown must keep a gap to the right edge of the screen (width %s)", viewportWidth)
            .isLessThanOrEqualTo(viewportWidth - MINIMUM_GAP_TO_SCREEN_EDGE);
    }

    /**
     * Asserts that the (opened) person group dropdown starts at the button and therefore opens to the
     * right. Opening to the left squeezes the dropdown into the gap between the button and the edge of
     * the screen, which is next to nothing for a button at the beginning of a heading.
     */
    public void showsPersonGroupDropdownOpeningToTheRight() {

        final BoundingBox dropdown = page.locator("#person-group-selection-popover").boundingBox();
        final BoundingBox button = getPersonGroupButtonLocator().boundingBox();

        org.assertj.core.api.Assertions.assertThat(dropdown.x)
            .describedAs("dropdown must start at the button, not at the edge of the screen")
            .isCloseTo(button.x, org.assertj.core.data.Offset.offset(1.0));
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
