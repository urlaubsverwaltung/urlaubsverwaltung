package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PaginationPage {

    private final String testIdPrefix;
    private final Page page;

    public PaginationPage(String testIdPrefix, Page page) {
        this.testIdPrefix = testIdPrefix;
        this.page = page;
    }

    public Locator getPageSizeSelectLocator() {
        return page.locator("[data-test-id=%s-page-size]".formatted(testIdPrefix));
    }

    public Locator getTotalElementsLocator() {
        return page.locator("[data-test-id=%s-page-total-elements]".formatted(testIdPrefix));
    }

    /**
     * @return Locator representing the pagination next-page-button
     */
    public Locator getNextPageButtonLocator() {
        return page.locator("[data-test-id=%s-page-button-next]".formatted(testIdPrefix));
    }

    /**
     * @return Locator representing the pagination previous-page-button
     */
    public Locator getPreviousPageButtonLocator() {
        return page.locator("[data-test-id=%s-page-button-previous]".formatted(testIdPrefix));
    }

    /**
     * @param pageZeroBased zero based page number
     * @return Locator representing the pagination pageNr button
     */
    public Locator getPageButtonLocator(int pageZeroBased) {
        return page.locator("[data-test-id=%s-page-button-%s]".formatted(testIdPrefix, pageZeroBased));
    }

    public void showsCurrentPage(int pageNumberZeroBased) {
        assertThat(getPageButtonLocator(pageNumberZeroBased)).hasAttribute("aria-current", "page");
    }
}
