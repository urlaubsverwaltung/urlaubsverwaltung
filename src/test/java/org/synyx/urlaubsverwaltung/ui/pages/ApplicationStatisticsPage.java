package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ApplicationStatisticsPage {

    private final Page page;
    private final DepartmentPickerPage departmentPicker;

    public ApplicationStatisticsPage(Page page) {
        this.page = page;
        this.departmentPicker = new DepartmentPickerPage(page);
    }

    public void navigate(int port) {
        // the test profile shows one row per page
        page.navigate("http://localhost:%d/web/application/statistics?size=20".formatted(port));
    }

    public DepartmentPickerPage getDepartmentPicker() {
        return departmentPicker;
    }

    public void search(String query) {
        page.locator("#search").fill(query);
    }

    /**
     * Asserts exactly these persons are shown in the statistics table, in order.
     *
     * @param niceNames first and last name of the shown persons
     */
    public void showsPersons(String... niceNames) {
        final Pattern[] rows = Arrays.stream(niceNames)
            .map(Pattern::compile)
            .toArray(Pattern[]::new);
        assertThat(page.locator("#application-statistic-table tbody tr")).hasText(rows);
    }
}
