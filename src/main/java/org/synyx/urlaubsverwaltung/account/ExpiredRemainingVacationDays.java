package org.synyx.urlaubsverwaltung.account;

import java.math.BigDecimal;

/**
 * The remaining vacation days of a holiday account that expired, with the numbers the person was notified about.
 *
 * @param account                          holiday account whose remaining vacation days expired
 * @param expiredRemainingVacationDays     remaining vacation days that expired
 * @param remainingVacationDaysNotExpiring remaining vacation days that do not expire and are still left
 * @param totalLeftVacationDays            vacation days still left in the year of the account
 */
record ExpiredRemainingVacationDays(
    Account account,
    BigDecimal expiredRemainingVacationDays,
    BigDecimal remainingVacationDaysNotExpiring,
    BigDecimal totalLeftVacationDays
) {
}
