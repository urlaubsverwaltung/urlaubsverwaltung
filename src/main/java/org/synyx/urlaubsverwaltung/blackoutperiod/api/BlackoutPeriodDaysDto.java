package org.synyx.urlaubsverwaltung.blackoutperiod.api;

import java.util.List;

public class BlackoutPeriodDaysDto {

    private final List<BlackoutPeriodDayDto> blackoutPeriods;

    BlackoutPeriodDaysDto(List<BlackoutPeriodDayDto> blackoutPeriods) {
        this.blackoutPeriods = blackoutPeriods;
    }

    public List<BlackoutPeriodDayDto> getBlackoutPeriods() {
        return blackoutPeriods;
    }
}
