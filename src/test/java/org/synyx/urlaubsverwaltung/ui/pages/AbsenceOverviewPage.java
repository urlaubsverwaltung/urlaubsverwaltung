package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AbsenceOverviewPage {

    private final Page page;
    private final DepartmentPickerPage departmentPicker;

    public AbsenceOverviewPage(Page page) {
        this.page = page;
        this.departmentPicker = new DepartmentPickerPage(page);
    }

    public void navigate(int port) {
        page.navigate("http://localhost:%d/web/absences".formatted(port));
    }

    public DepartmentPickerPage getDepartmentPicker() {
        return departmentPicker;
    }

    public void selectYear(String year) {
        page.locator("#yearSelect").selectOption(year);
    }

    /**
     * Asserts exactly these persons are shown in the overview, in order.
     *
     * @param firstNames first names of the shown persons - used as regex, the pattern is evaluated as JavaScript regex
     *                   in the browser, which knows no {@code \Q…\E} quoting
     */
    public void showsPersons(String... firstNames) {
        final Pattern[] rows = Arrays.stream(firstNames)
            .map(firstName -> Pattern.compile("^\\s*" + firstName + "\\b"))
            .toArray(Pattern[]::new);
        assertThat(page.locator("tbody.vacationOverview-tbody tr th:nth-of-type(2)")).hasText(rows);
    }
}
