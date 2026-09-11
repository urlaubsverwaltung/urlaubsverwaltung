package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;

import java.util.List;
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

    /**
     * Asserts that every line of the (wrapped) person group button label starts at the same x. A button
     * is centered by the user agent stylesheet, which only shows once its label wraps.
     */
    public void showsPersonGroupWithEveryLineLeftAligned() {

        final List<?> clientRectLefts = (List<?>) page.evaluate("""
            () => {
              const button = document.querySelector('#person-group-popover-button');
              const label = [...button.childNodes].find(node => node.nodeType === Node.TEXT_NODE && node.textContent.trim());
              const range = document.createRange();
              range.selectNodeContents(label);
              return [...range.getClientRects()].map(rect => Math.round(rect.left));
            }
            """);

        final List<Integer> lineStarts = clientRectLefts.stream().map(left -> ((Number) left).intValue()).toList();

        org.assertj.core.api.Assertions.assertThat(lineStarts)
            .describedAs("the label has to wrap, otherwise this does not assert anything")
            .hasSizeGreaterThan(1);

        org.assertj.core.api.Assertions.assertThat(lineStarts)
            .describedAs("every line of the wrapped label must start at the left edge of the button")
            .containsOnly(lineStarts.getFirst());
    }

    /**
     * Asserts that the chevron of the person group button keeps its shape. As a flex item it shrinks
     * along with the button, which squashes it to a stroke next to a long label.
     */
    public void showsPersonGroupButtonWithUndistortedChevron() {

        final BoundingBox chevron = page.locator("#person-group-popover-button > svg").boundingBox();

        org.assertj.core.api.Assertions.assertThat(chevron.width)
            .describedAs("the chevron must not be squashed, it is %sx%s", chevron.width, chevron.height)
            .isCloseTo(chevron.height, org.assertj.core.data.Offset.offset(0.5));
    }

    /**
     * Asserts that the person group button and the year button are separated by a gap once they no
     * longer fit next to each other and are stacked.
     */
    public void showsGapBetweenStackedPersonGroupAndYearButton() {

        final BoundingBox personGroup = getPersonGroupButtonLocator().boundingBox();
        final BoundingBox year = page.locator("#year-selector-popover-button").boundingBox();
        final double gap = year.y - (personGroup.y + personGroup.height);

        org.assertj.core.api.Assertions.assertThat(year.y)
            .describedAs("the buttons have to be stacked, otherwise this does not assert anything")
            .isGreaterThan(personGroup.y);

        org.assertj.core.api.Assertions.assertThat(gap)
            .describedAs("stacked buttons must be separated by a gap")
            .isGreaterThanOrEqualTo(4);
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
