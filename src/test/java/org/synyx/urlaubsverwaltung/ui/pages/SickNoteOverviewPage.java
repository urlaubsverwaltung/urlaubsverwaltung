package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SickNoteOverviewPage {

    private static final String TABLE_SELECTOR = "[data-test-id=sick-notes-table]";

    private final Page page;
    private final Locale locale;
    private final MessageSource messageSource;

    public SickNoteOverviewPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.locale = locale;
        this.messageSource = messageSource;
    }

    public void waitForVisible() {
        page.waitForSelector(TABLE_SELECTOR);
    }

    public void showsSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = msg("sicknotes.daysOverview.sickDays.number");
        final String sickDaysAubText = msg("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate});
        showsPersonRow(firstName, lastName, sickDays + " " + sickDaysText, sickDaysAubText, daysWithIncapacityCertificate);
    }

    public void showsChildSickNoteStatistic(String firstName, String lastName, int sickDays, int daysWithIncapacityCertificate) {
        final String sickDaysText = msg("sicknotes.daysOverview.sickDays.child.number");
        final String sickDaysAubText = msg("overview.sicknotes.sickdays.aub", new Object[]{daysWithIncapacityCertificate});
        showsPersonRow(firstName, lastName, sickDays + " " + sickDaysText, sickDaysAubText, daysWithIncapacityCertificate);
    }

    private String msg(String key) {
        return msg(key, new Object[]{});
    }

    private String msg(String key, Object[] args) {
        return messageSource.getMessage(key, args, locale);
    }

    private void showsPersonRow(String firstName, String lastName, String sickDaysText, String aubText, int daysWithIncapacityCertificate) {

        final Locator personRowLocator = page.locator("tr")
            // person
            .filter(new Locator.FilterOptions().setHasText(firstName))
            .filter(new Locator.FilterOptions().setHasText(lastName))
            // sick days
            .filter(new Locator.FilterOptions().setHasText(sickDaysText))
            // AUB
            .filter(daysWithIncapacityCertificate == 0
                ? new Locator.FilterOptions().setHasNotText(aubText)
                : new Locator.FilterOptions().setHasText(aubText)
            );

        assertThat(page.locator(TABLE_SELECTOR).locator(personRowLocator)).isVisible();
    }
}
