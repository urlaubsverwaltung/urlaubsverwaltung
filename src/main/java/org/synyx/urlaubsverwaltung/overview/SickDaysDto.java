package org.synyx.urlaubsverwaltung.overview;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.math.BigDecimal.ZERO;
import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.TOTAL;
import static org.synyx.urlaubsverwaltung.overview.SickDaysDto.SickDayType.WITH_AUB;

/**
 * Represents number of days for specific sick note types.
 */
public class SickDaysDto {

    enum SickDayType {
        TOTAL,
        WITH_AUB
    }

    private final Map<String, BigDecimal> days;

    SickDaysDto() {
        days = new HashMap<>();
        days.put(TOTAL.name(), ZERO);
        days.put(WITH_AUB.name(), ZERO);
    }

    public Map<String, BigDecimal> getDays() {
        return days;
    }

    void addDays(SickDayType type, BigDecimal days) {
        final BigDecimal addedDays = this.days.get(type.name()).add(days);
        this.days.put(type.name(), addedDays);
    }
}
