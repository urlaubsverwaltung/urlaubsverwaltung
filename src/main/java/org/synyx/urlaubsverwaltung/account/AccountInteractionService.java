package org.synyx.urlaubsverwaltung.account;

import org.springframework.lang.Nullable;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Provides interactions with {@link Account}s like creating or editing.
 */
public interface AccountInteractionService {

    /**
     * Creates a default {@link Account} for given person
     * <p>
     * Assuming 20 days of holiday (based on http://www.gesetze-im-internet.de/burlg/__3.html with five days work per week).
     * Calculates the number of remaining days based on account creation day.
     *
     * @param person to setup default account
     */
    void createDefaultAccount(Person person);

    /**
     * Creates a {@link Account} with the given parameters.
     *
     * @param person                           defines the owner of the holidays account
     * @param validFrom                        defines the start of the validity period, e.g. 1.1.2012
     * @param validTo                          defines the end of the validity period, e.g. 31.12.2012
     * @param expiryDate                       defines the expiry date of vacation days, e.g. 01.04.2012
     * @param annualVacationDays               defines number of annual vacation days
     * @param actualVacationDays               the actual vacation days for the period
     * @param remainingVacationDays            defines the number of remaining vacation days from the last year
     * @param remainingVacationDaysNotExpiring defines the number of remaining vacation days that do not expire on the expiry date
     * @param comment                          comment to changes to the annual vacation days
     * @return the created holidays account
     */
    Account updateOrCreateHolidaysAccount(Person person, LocalDate validFrom, LocalDate validTo, Boolean doRemainingVacationDaysExpire,
                                          @Nullable LocalDate expiryDate, BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                          BigDecimal remainingVacationDays, BigDecimal remainingVacationDaysNotExpiring,
                                          String comment);

    /**
     * Edits the given {@link Account} with the given params.
     *
     * @param account                          to be edited
     * @param validFrom                        defines the start of the validity period, e.g. 1.1.2012
     * @param validTo                          defines the end of the validity period, e.g. 31.12.2012
     * @param doRemainingVacationDaysExpire    {@code true}/{@code false} to define account specifics, {@code null} to use global settings.
     * @param expiryDate                       defines the date when the remaining vacation days will expire e.g. 1.4.2012
     * @param annualVacationDays               defines number of annual vacation days
     * @param actualVacationDays               the actual vacation days for the period
     * @param remainingVacationDays            defines the number of remaining vacation days from the last year
     * @param remainingVacationDaysNotExpiring defines the number of remaining vacation days that do not expire on the expiry date
     * @param comment                          comment to changes to the annual vacation days
     * @return the updated holidays account
     */
    Account editHolidaysAccount(Account account, LocalDate validFrom, LocalDate validTo, @Nullable Boolean doRemainingVacationDaysExpire,
                                @Nullable LocalDate expiryDate, BigDecimal annualVacationDays, BigDecimal actualVacationDays,
                                BigDecimal remainingVacationDays, @Nullable BigDecimal remainingVacationDaysNotExpiring,
                                String comment);

    /**
     * Auto-creates a new {@link Account} or updates the existing {@link Account} for the next year, based on the information of the given reference account.
     *
     * @param referenceAccount to get the information about annual vacation days, left vacation days etc.
     * @return the created/updated holidays account
     */
    Account autoCreateOrUpdateNextYearsHolidaysAccount(Account referenceAccount);

    /**
     * Updates the remaining vacation days of all {@link Account}s that follow the {@link Account} of the given year. Updating is stopped when there is no next year's
     * {@link Account}.
     *
     * @param year   to start the update for
     * @param person to update the remaining vacation days for
     */
    void updateRemainingVacationDays(int year, Person person);


    /**
     * Deletes all {@link Account}s in the database of person.
     *
     * @param person the person which accounts will be deleted
     */
    void deleteAllByPerson(Person person);
}
