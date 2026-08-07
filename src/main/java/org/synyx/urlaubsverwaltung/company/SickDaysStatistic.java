package org.synyx.urlaubsverwaltung.company;

import java.math.BigDecimal;

record SickDaysStatistic(
    HealthRate healthRate,
    BigDecimal totalNumberOfAllSickNotes,
    BigDecimal shouldWorkDays
) {

    record HealthRate(double value) {

        public static HealthRate of(BigDecimal bigDecimal) {
            return new HealthRate(bigDecimal.doubleValue() / 100);
        }
    }
}
