package org.synyx.urlaubsverwaltung.core.settings;

import javax.persistence.Column;
import javax.persistence.Embeddable;


/**
 * Settings to sync absences with a calendar provider.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Embeddable
public class CalendarSettings {

    private ExchangeCalendarSettings exchangeCalendarSettings;

    @Column(name = "calendar_workDayBeginHour")
    private Integer workDayBeginHour = 8;

    @Column(name = "calendar_workDayEndHour")
    private Integer workDayEndHour = 17;

    public ExchangeCalendarSettings getExchangeCalendarSettings() {

        if (exchangeCalendarSettings == null) {
            exchangeCalendarSettings = new ExchangeCalendarSettings();
        }

        return exchangeCalendarSettings;
    }


    public void setExchangeCalendarSettings(ExchangeCalendarSettings exchangeCalendarSettings) {

        this.exchangeCalendarSettings = exchangeCalendarSettings;
    }


    public Integer getWorkDayBeginHour() {

        return workDayBeginHour;
    }


    public void setWorkDayBeginHour(Integer workDayBeginHour) {

        this.workDayBeginHour = workDayBeginHour;
    }


    public Integer getWorkDayEndHour() {

        return workDayEndHour;
    }


    public void setWorkDayEndHour(Integer workDayEndHour) {

        this.workDayEndHour = workDayEndHour;
    }
}
