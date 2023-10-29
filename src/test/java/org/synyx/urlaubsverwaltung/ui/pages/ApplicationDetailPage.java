package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Locale;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class ApplicationDetailPage {

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public ApplicationDetailPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisibleForPerson(String username) {
        return page.title().equals(title(username));
    }

    public boolean showsApplicationCreatedInfo() {
        final String text = messageSource.getMessage("application.action.apply.success", new Object[]{}, locale);
        return page.getByText(text).isVisible();
    }

    public boolean showsReplacement(Person person) {
        final Locator element = page.locator("[data-test-id=holiday-replacement-list]");
        return element.textContent().contains(person.getNiceName());
    }

    public void selectEdit() {
        page.waitForResponse(Response::ok, () -> page.locator("[data-test-id=application-edit-button]").click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    private String title(String username) {
        return messageSource.getMessage("application.data.header.title", new Object[]{username}, locale);
    }
}
