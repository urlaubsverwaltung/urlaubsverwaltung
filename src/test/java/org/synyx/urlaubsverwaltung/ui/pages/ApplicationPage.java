package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import org.synyx.urlaubsverwaltung.person.Person;

import java.time.LocalDate;
import java.util.List;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static java.time.format.DateTimeFormatter.ofPattern;

public class ApplicationPage {

    private static final String FROM_INPUT_SELECTOR = "#from";
    private static final String SUBMIT_SELECTOR = "button#apply-application";
    private static final String VACATION_TYPE_SELECT_SELECTOR = "[data-test-id=vacation-type-select]";
    private static final String DAY_LENGTH_FULL_SELECTOR = "[data-test-id=day-length-full]";
    private static final String DAY_LENGTH_MORNING_SELECTOR = "[data-test-id=day-length-morning]";
    private static final String DAY_LENGTH_NOON_SELECTOR = "[data-test-id=day-length-noon]";

    private final Page page;

    public ApplicationPage(Page page) {
        this.page = page;
    }

    public boolean isVisible() {
        return page.locator(VACATION_TYPE_SELECT_SELECTOR).isVisible()
            && page.locator(FROM_INPUT_SELECTOR).isVisible();
    }

    public void from(LocalDate date) {
        final String dateString = ofPattern("d.M.yyyy").format(date);
        page.locator(FROM_INPUT_SELECTOR).fill(dateString);
    }

    public Locator vacationTypeSelect() {
        return page.locator(VACATION_TYPE_SELECT_SELECTOR);
    }

    public void selectVacationTypeOfName(String vacationTypeName) {
        final Locator option = page.locator("option").and(page.getByText(vacationTypeName));
        vacationTypeSelect().selectOption(option.elementHandle());
    }

    public void reason(String reasonText) {
        page.locator("[data-test-id=reason]").fill(reasonText);
    }

    /**
     * selected the given person in the  replacement select box.
     * Note that this does not submit the form! Maybe there is JavaScript loaded which does it, though.
     *
     * @param person person that should be selected
     */
    public void selectReplacement(Person person) {
        final Locator element = page.locator("[data-test-id=holiday-replacement-select]");
        element.selectOption(String.valueOf(person.getId()));
    }

    public void setCommentForReplacement(Person person, String comment) {
        final HolidayReplacementRowElement holidayReplacementRow = getHolidayReplacementRow(person);

        if (holidayReplacementRow == null) {
            throw new IllegalStateException("could not find replacement row for the given person.");
        }

        final Locator textarea = holidayReplacementRow.rowElement.locator("textarea");
        textarea.fill("");
        textarea.fill(comment);

        // for whatever reasons we have to blur the textarea afterwards
        // otherwise a single form submit click doesn't submit the form...
        page.locator(":focus").blur();
    }

    /**
     * DayLength can be enabled/disabled in the settings. This method checks if the input elements are available in the application page.
     *
     * @return {@code true} when inputs are visible, {@code false} otherwise.
     */
    public boolean showsDayLengthInputs() {
        return page.locator(DAY_LENGTH_FULL_SELECTOR).isVisible()
            && page.locator(DAY_LENGTH_MORNING_SELECTOR).isVisible()
            && page.locator(DAY_LENGTH_NOON_SELECTOR).isVisible();
    }

    /**
     * Checks if the given person is visible at the given position of added replacements.
     *
     * @param person   person that should be visible
     * @param position the position to check against. starts with 1.
     * @return <code>true</code> if the person is visible at the given position, <code>false</code> otherwise.
     */
    public boolean showsAddedReplacementAtPosition(Person person, int position) {
        return this.showsAddedReplacementAtPosition(person, position, "");
    }

    /**
     * Checks if the given person is visible at the given position of added replacements and if it has the given comment.
     *
     * @param person   person that should be visible
     * @param position the position to check against. starts with 1.
     * @param comment  the comment for the replacement
     * @return <code>true</code> if the person is visible at the given position, <code>false</code> otherwise.
     */
    public boolean showsAddedReplacementAtPosition(Person person, int position, String comment) {
        if (position < 1) {
            throw new IllegalArgumentException("position must be greater 0.");
        }

        final HolidayReplacementRowElement holidayReplacementRow = getHolidayReplacementRow(person);
        if (holidayReplacementRow == null) {
            return false;
        }

        final Locator row = holidayReplacementRow.rowElement;
        final int rowPosition = holidayReplacementRow.position;

        if (position != rowPosition) {
            return false;
        }

        final Locator textarea = row.locator("textarea");
        return textarea.inputValue().equals(comment);
    }

    public void submit() {
        page.waitForResponse(Response::ok, () -> page.locator(SUBMIT_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    public boolean showsFromError() {
        return page.locator("[data-test-id=from-error]").isVisible();
    }

    public boolean showsToError() {
        return page.locator("[data-test-id=to-error]").isVisible();
    }

    public boolean showsReason() {
        return page.locator("[data-test-id=reason]").isVisible();
    }

    public boolean showsReasonError() {
        return page.locator("[data-test-id=reason-error]").isVisible();
    }

    public boolean showsOvertimeReductionHours() {
        return page.locator("[data-test-id=overtime-hours]").isVisible();
    }
    public boolean showsOvertimeReductionHoursError() {
        return page.locator("[data-test-id=overtime-hours-error]").isVisible();
    }

    public void overtimeReductionHours(double hours) {
        page.locator("[data-test-id=overtime-hours]").fill(String.valueOf(hours));
    }

    public void overtimeReductionMinutes(int minutes) {
        page.locator("[data-test-id=overtime-minutes]").fill(String.valueOf(minutes));
    }

    private HolidayReplacementRowElement getHolidayReplacementRow(Person person) {

        final List<Locator> rows = page.locator("[data-test-id=holiday-replacement-row]").all();

        for (int i = 0; i < rows.size(); i++) {
            final Locator row = rows.get(i);
            final List<Locator> hiddenInputElements = row.locator("input[type=hidden]").all();

            final boolean isRowOfPerson = hiddenInputElements.stream().anyMatch(input -> {
                final String name = input.getAttribute("name");
                final String value = input.inputValue();
                return name.startsWith("holidayReplacements[")
                    && name.endsWith("].person")
                    && value.equals(String.valueOf(person.getId()));
            });

            if (isRowOfPerson) {
                return new HolidayReplacementRowElement(row, i + 1);
            }
        }

        return null;
    }

    private record HolidayReplacementRowElement(Locator rowElement, int position) {
    }
}
