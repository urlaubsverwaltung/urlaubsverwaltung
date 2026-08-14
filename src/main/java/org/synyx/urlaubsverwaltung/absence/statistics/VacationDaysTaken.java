package org.synyx.urlaubsverwaltung.absence.statistics;

import org.synyx.urlaubsverwaltung.account.Account;
import org.synyx.urlaubsverwaltung.account.VacationDaysLeft;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static org.synyx.urlaubsverwaltung.util.CalcUtil.isZero;

/**
 * Splits vacation entitlement into what has already been taken or planned versus what is still valid, for a single
 * stichtag.
 *
 * <p>
 * A pure calculation without Spring or database concerns of its own — accounts and their derived
 * {@link VacationDaysLeft} are handed in already resolved; fetching them is Task 04's job. Which stichtag applies
 * for a given year is decided by the caller, not here.
 */
class VacationDaysTaken {

    private static final int PERCENTAGE_SCALE = 3;
    private static final int AVERAGE_SCALE = 2;

    /**
     * Calculates the taken/planned vacation days, the still-valid entitlement, the percentage of the former
     * relative to the latter, the average taken/planned days per person and the sum of expired remaining vacation
     * days, for the given stichtag.
     *
     * <p>
     * Persons without a vacation account for the year are expected to already be absent from
     * {@code vacationDaysLeftByAccount} — they do not influence any of the returned numbers.
     *
     * @param stichtag                    the date to evaluate expiry and entitlement against
     * @param vacationDaysLeftByAccount   each person's vacation account together with their derived left vacation
     *                                    days
     * @return the aggregated result for the given stichtag
     */
    VacationDaysTakenResult calculate(LocalDate stichtag, Map<Account, VacationDaysLeft> vacationDaysLeftByAccount) {

        BigDecimal takenSum = ZERO;
        BigDecimal validEntitlementSum = ZERO;
        BigDecimal expiredSum = ZERO;

        for (Map.Entry<Account, VacationDaysLeft> entry : vacationDaysLeftByAccount.entrySet()) {

            final Account account = entry.getKey();
            final VacationDaysLeft vacationDaysLeft = entry.getValue();

            final boolean doRemainingVacationDaysExpire = account.doRemainingVacationDaysExpire();
            final LocalDate expiryDate = account.getExpiryDate();

            // the entitlement as it was ever granted, before usage is subtracted - VacationDaysLeft only ever
            // exposes what's *left*, so the gross numbers have to come from the account itself.
            final BigDecimal grossEntitlement = account.getActualVacationDays().add(account.getRemainingVacationDays());
            final BigDecimal expired = vacationDaysLeft.getExpiredRemainingVacationDays(stichtag, expiryDate);
            final BigDecimal validEntitlement = grossEntitlement.subtract(expired);

            final BigDecimal leftVacationDays = vacationDaysLeft.getLeftVacationDays(stichtag, doRemainingVacationDaysExpire, expiryDate);
            final BigDecimal taken = validEntitlement.subtract(leftVacationDays);

            takenSum = takenSum.add(taken);
            validEntitlementSum = validEntitlementSum.add(validEntitlement);
            expiredSum = expiredSum.add(expired);
        }

        final BigDecimal percentage = isZero(validEntitlementSum)
            ? ZERO
            : takenSum.divide(validEntitlementSum, PERCENTAGE_SCALE, HALF_UP).multiply(valueOf(100));

        final int personCount = vacationDaysLeftByAccount.size();
        final BigDecimal averagePerPerson = personCount == 0
            ? ZERO
            : takenSum.divide(valueOf(personCount), AVERAGE_SCALE, HALF_UP);

        return new VacationDaysTakenResult(takenSum, validEntitlementSum, percentage, averagePerPerson, expiredSum);
    }
}
