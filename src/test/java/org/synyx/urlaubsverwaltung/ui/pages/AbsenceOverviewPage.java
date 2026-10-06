package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AbsenceOverviewPage {

    private final Page page;

    public AbsenceOverviewPage(Page page) {
        this.page = page;
    }

    public void navigate(int port) {
        page.navigate("http://localhost:%d/web/absences".formatted(port));
    }

    public Locator getDepartmentPickerButtonLocator() {
        return page.locator("#department-picker");
    }

    public Locator getDepartmentPickerPopoverLocator() {
        return page.locator("#department-picker-popover");
    }

    public void openDepartmentPicker() {
        getDepartmentPickerButtonLocator().click();
        assertThat(getDepartmentPickerPopoverLocator()).isVisible();
    }

    public void checkDepartment(String departmentName) {
        getDepartmentPickerPopoverLocator()
            .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName(departmentName))
            .check();
    }

    public void checkAllPersons() {
        getDepartmentPickerPopoverLocator()
            .getByRole(AriaRole.CHECKBOX, new Locator.GetByRoleOptions().setName("Alle Personen"))
            .check();
    }

    public void confirmDepartmentPicker() {
        getDepartmentPickerPopoverLocator()
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Bestätigen"))
            .click();
    }

    /**
     * @return computed colour of the caret - the caret is a mask on the button's {@code ::after}, painted by its background
     */
    public String getDepartmentPickerCaretColor() {
        return (String) getDepartmentPickerButtonLocator().evaluate("button => getComputedStyle(button, '::after').backgroundColor");
    }

    /**
     * @param customProperty a colour custom property of the theme, e.g. {@code --color-zinc-50}
     * @return the property computed as a colour on this page
     */
    public String colorOf(String customProperty) {
        return (String) page.evaluate("""
            property => {
              const probe = document.createElement("div");
              probe.style.backgroundColor = `var(${property})`;
              document.body.append(probe);
              const color = getComputedStyle(probe).backgroundColor;
              probe.remove();
              return color;
            }""", customProperty);
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
