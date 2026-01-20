package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SickNoteExtensionPage {

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    // pattern does not depend on locale currently. the user cannot customize it.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_FULL_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy");

    public SickNoteExtensionPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisible() {
        return page.title().equals(messageSource.getMessage("sicknote.extend.header.title", null, locale));
    }

    public void setCustomNextEndDate(LocalDate nextEndDate) {
        final String nextEndDateValue = DATE_FORMATTER.format(nextEndDate);
        // this only works for initialised datepicker (fails with native date input element)
        page.locator("[data-test-id=sicknote-custom-next-end-date-input]").fill(nextEndDateValue);
    }

    public void showsExtensionPreview(LocalDate startDate, LocalDate nextEndDate) {

        final String startLabel = messageSource.getMessage("sicknote.extend.preview.new.start.label", null, locale);
        final String startValue = DATE_FULL_FORMATTER.withLocale(locale).format(startDate);
        final String nextEndLabel = messageSource.getMessage("sicknote.extend.preview.new.end.label", null, locale);
        final String nextEndValue = DATE_FULL_FORMATTER.withLocale(locale).format(nextEndDate);

        final Locator locator = page.locator("[data-test-id=sick-note-extension-next-preview]");
        assertThat(locator).containsText("%s %s %s %s".formatted(startLabel, startValue, nextEndLabel, nextEndValue));
    }
    /**
     * Submits the form, does not wait for anything. You have to wait for the next visible page yourself!
     */
    public void submit() {
        page.locator("[data-test-id=extension-submit-button]").click();
    }
}
