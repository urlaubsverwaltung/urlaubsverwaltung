package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.options.AriaRole.BUTTON;
import static com.microsoft.playwright.options.AriaRole.CHECKBOX;

public class DepartmentPickerPage {

    private final Page page;

    public DepartmentPickerPage(Page page) {
        this.page = page;
    }

    public Locator getButtonLocator() {
        return page.locator("#department-picker");
    }

    public Locator getPopoverLocator() {
        return page.locator("#department-picker-popover");
    }

    public Locator getDepartmentCheckboxLocator(String departmentName) {
        return getPopoverLocator().getByRole(CHECKBOX, new Locator.GetByRoleOptions().setName(departmentName));
    }

    public void open() {
        getButtonLocator().click();
        assertThat(getPopoverLocator()).isVisible();
    }

    public void checkDepartment(String departmentName) {
        getDepartmentCheckboxLocator(departmentName).check();
    }

    public void checkAllPersons() {
        getDepartmentCheckboxLocator("Alle Personen").check();
    }

    public void confirm() {
        getPopoverLocator()
            .getByRole(BUTTON, new Locator.GetByRoleOptions().setName("Bestätigen"))
            .click();
    }
}
