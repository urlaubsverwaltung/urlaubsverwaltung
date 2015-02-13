package org.synyx.urlaubsverwaltung.restapi;

import java.util.List;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
class PublicHolidayListResponse {

    private List<PublicHolidayResponse> publicHolidays;

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
