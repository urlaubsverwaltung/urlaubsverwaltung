package org.synyx.urlaubsverwaltung.restapi.holiday;

import de.jollyday.Holiday;

import java.math.BigDecimal;

import java.util.Locale;


/**
 * @author  Aljona Murygina <murygina@synyx.de>
 */
class PublicHolidayResponse {

    private String date;
    private String description;
    private BigDecimal dayLength;

    PublicHolidayResponse(Holiday holiday, BigDecimal dayLength) {

        this.date = holiday.getDate().toString();
        this.description = holiday.getDescription(Locale.GERMAN);
        this.dayLength = dayLength;
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


    public BigDecimal getDayLength() {

        return dayLength;
    }


    public void setDayLength(BigDecimal dayLength) {

        this.dayLength = dayLength;
    }
}
