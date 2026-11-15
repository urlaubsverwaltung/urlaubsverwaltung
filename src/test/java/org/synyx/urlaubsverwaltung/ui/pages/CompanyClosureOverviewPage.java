package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.options.AriaRole.LINK;

public class CompanyClosureOverviewPage {

    private final Page page;

    public CompanyClosureOverviewPage(Page page) {
        this.page = page;
    }

    public static String url(int port) {
        return "http://localhost:%d/web/company-closure".formatted(port);
    }

    public void open(int port) {
        page.navigate(url(port));
    }

    public void clickCreate() {
        page.getByRole(LINK, new Page.GetByRoleOptions().setName("Neuer Tag")).click();
    }

    /**
     * @param name name of the closure day as the overview shows it
     */
    public void clickEdit(String name) {
        page.locator(".company-closure-list__item")
            .filter(new Locator.FilterOptions().setHas(page.getByText(name, new Page.GetByTextOptions().setExact(true)).first()))
            .getByRole(LINK, new Locator.GetByRoleOptions().setName("Bearbeiten").setExact(false))
            .click();
    }

    public Locator closureDays() {
        return page.locator(".company-closure-list");
    }
}
