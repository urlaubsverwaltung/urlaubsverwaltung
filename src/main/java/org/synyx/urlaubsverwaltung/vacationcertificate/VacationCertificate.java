package org.synyx.urlaubsverwaltung.vacationcertificate;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Everything the vacation certificate (§ 6 Abs. 2 BUrlG) states for one calendar year that can be computed.
 *
 * @param year                 calendar year of the certificate
 * @param entitlement          vacation entitlement arisen in this year
 * @param workingDaysPerWeek   working days per week at the end of the employment within the year, empty without working time
 * @param grantedPeriods       granted vacation periods of the year, sorted by start date
 * @param grantedTotal         sum of the working days of the granted periods
 * @param grantedFromRemaining part of {@code grantedTotal} taken from the remaining vacation of the previous year
 * @param hasOpenApplications  whether there are holiday applications of the year not decided yet
 */
record VacationCertificate(
    Year year,
    BigDecimal entitlement,
    Optional<BigDecimal> workingDaysPerWeek,
    List<GrantedVacationPeriod> grantedPeriods,
    BigDecimal grantedTotal,
    BigDecimal grantedFromRemaining,
    boolean hasOpenApplications
) {
}
