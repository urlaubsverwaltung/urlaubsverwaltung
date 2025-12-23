package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.person.Person;

import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ApplicationDetailPage {

    private static final String DATA_PAGE = "main[data-page='application-detail']";

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public ApplicationDetailPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public void waitForVisible() {
        page.waitForSelector(DATA_PAGE);
    }

    public boolean isVisibleForPerson(String username) {
        return page.title().equals(title(username));
    }

    public void showsApplicationCreatedInfo() {
        final String text = messageSource.getMessage("application.action.apply.success", new Object[]{}, locale);
        assertThat(page.getByText(text)).isVisible();
    }

    public Locator replacementLocator(Person person) {
        final Page.LocatorOptions hasText = new Page.LocatorOptions().setHasText(person.getNiceName());
        return page.locator("[data-test-id=holiday-replacement-list]", hasText);
    }

    /**
     * Clicks the link, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void clickEdit() {
        page.locator("[data-test-id=application-edit-button]").click();
    }

    private String title(String username) {
        return messageSource.getMessage("application.data.header.title", new Object[]{username}, locale);
    }
}
