package org.synyx.urlaubsverwaltung.restapi;

import de.jollyday.Holiday;
import org.joda.time.LocalDate;
import org.json4s.ext._LocalDate;

import java.util.Locale;

/**
 * @author Aljona Murygina <murygina@synyx.de>
 */
class PublicHolidayResponse {

    private String date;
    private String description;

    PublicHolidayResponse(Holiday holiday) {

        this.date = holiday.getDate().toString();
        this.description = holiday.getDescription(Locale.GERMAN);

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
