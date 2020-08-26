package org.synyx.urlaubsverwaltung.holiday;

import java.util.ArrayList;
import java.util.List;

class PublicHolidaysDto {

    private List<PublicHolidayDto> publicHolidays;

    PublicHolidaysDto() {
        this.publicHolidays = new ArrayList<>();
    }

    PublicHolidaysDto(List<PublicHolidayDto> publicHolidays) {
        this.publicHolidays = publicHolidays;
    }

    public List<PublicHolidayDto> getPublicHolidays() {
        return publicHolidays;
    }

    public void setPublicHolidays(List<PublicHolidayDto> publicHolidays) {
        this.publicHolidays = publicHolidays;
    }
}
