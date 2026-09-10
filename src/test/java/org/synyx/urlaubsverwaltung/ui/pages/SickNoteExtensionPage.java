package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class SickNoteExtensionPage {

    // the `duet-date-picker` prefix matches only the hydrated datepicker, not the plain `input[date]` the server renders.
    private static final String DUET_CUSTOM_NEXT_END_DATE_SELECTOR =
        "duet-date-picker [data-test-id=sicknote-custom-next-end-date-input]";
    // the datepicker itself carries the boundaries, its inner input carries the test id
    private static final String DUET_CUSTOM_NEXT_END_DATE_PICKER_SELECTOR =
        "duet-date-picker:has([data-test-id=sicknote-custom-next-end-date-input])";

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    // pattern does not depend on locale currently. the user cannot customize it.
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATE_FULL_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy");
    // the format `duet-date-picker` renders into its input, see the `de` datepicker localisation
    private static final DateTimeFormatter DATE_PICKER_FORMATTER = DateTimeFormatter.ofPattern("d.M.yyyy");
    // the screen reader label of a day of the currently shown month, hard coded by duet-date-picker
    private static final DateTimeFormatter DATE_PICKER_DAY_FORMATTER = DateTimeFormatter.ofPattern("d. MMMM");

    public SickNoteExtensionPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisible() {
        return page.title().contains(messageSource.getMessage("sicknote.extend.header.title", null, locale));
    }

    /**
     * Waits for the page and its datepicker to be ready to interact with.
     */
    public void waitForVisible() {
        page.waitForCondition(this::isVisible);
        page.waitForSelector(DUET_CUSTOM_NEXT_END_DATE_SELECTOR);
    }

    public void setCustomNextEndDate(LocalDate nextEndDate) {
        final String nextEndDateValue = DATE_FORMATTER.format(nextEndDate);
        page.locator(DUET_CUSTOM_NEXT_END_DATE_SELECTOR).fill(nextEndDateValue);
    }

    /**
     * Picks the next end date in the datepicker instead of typing it. Does not wait for anything, the preview
     * is rendered asynchronously.
     *
     * @param nextEndDate date to pick, has to be in the month the datepicker opens with
     */
    public void pickCustomNextEndDate(LocalDate nextEndDate) {
        page.locator(DUET_CUSTOM_NEXT_END_DATE_PICKER_SELECTOR + " button.duet-date__toggle").click();
        final String dayLabel = DATE_PICKER_DAY_FORMATTER.withLocale(locale).format(nextEndDate);
        page.locator("%s .duet-date__day:has(.duet-date__vhidden:text-is('%s'))"
            .formatted(DUET_CUSTOM_NEXT_END_DATE_PICKER_SELECTOR, dayLabel)).click();
    }

    /**
     * Asserts the date the datepicker of the custom next end date shows.
     */
    public void showsCustomNextEndDate(LocalDate nextEndDate) {
        assertThat(page.locator(DUET_CUSTOM_NEXT_END_DATE_SELECTOR))
            .hasValue(DATE_PICKER_FORMATTER.withLocale(locale).format(nextEndDate));
    }

    /**
     * Extends the sick note by one work day following its end date.
     * Does not wait for anything, the preview is rendered asynchronously.
     */
    public void clickPlusOneWorkday() {
        page.locator("button[name=extend][value='1']").click();
    }

    /**
     * Asks for the preview of the custom date. Does not wait for anything, the preview is rendered
     * asynchronously.
     */
    public void clickCustomDatePreview() {
        page.locator("#submit-date-button").click();
    }

    /**
     * Asserts that extending until the end of the week is not offered, which is the case when the sick note
     * does not end before it.
     */
    public void showsNoEndOfWeekOption() {
        assertThat(page.locator("button[name=extend][value='end-of-week']")).hasCount(0);
    }

    /**
     * Asserts the first date the datepicker allows to be picked as the next end date.
     *
     * @param earliestNextEndDate first selectable date, the day after the end of the sick note
     */
    public void showsEarliestSelectableNextEndDate(LocalDate earliestNextEndDate) {
        final String earliestValue = ISO_LOCAL_DATE.format(earliestNextEndDate);
        assertThat(page.locator(DUET_CUSTOM_NEXT_END_DATE_PICKER_SELECTOR)).hasAttribute("min", earliestValue);
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
