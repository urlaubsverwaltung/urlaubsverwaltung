package org.synyx.urlaubsverwaltung.restapi.holiday;

import java.util.ArrayList;
import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class PublicHolidayListResponse {

    private List<PublicHolidayResponse> publicHolidays;

    PublicHolidayListResponse() {

        this.publicHolidays = new ArrayList<>();
    }


    PublicHolidayListResponse(List<PublicHolidayResponse> publicHolidays) {

        this.publicHolidays = publicHolidays;
    }

    public List<PublicHolidayResponse> getPublicHolidays() {

        return publicHolidays;
    }


    public void setPublicHolidays(List<PublicHolidayResponse> publicHolidays) {

        this.publicHolidays = publicHolidays;
    }
}
