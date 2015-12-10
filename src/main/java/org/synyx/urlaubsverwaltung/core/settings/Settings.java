package org.synyx.urlaubsverwaltung.core.settings;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.synyx.urlaubsverwaltung.core.period.DayLength;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Represents the settings / business rules for the application.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Entity
public class Settings extends AbstractPersistable<Integer> {

    private AbsenceSettings absenceSettings;

    /**
     * Defines the working duration for Christmas Eve and New Years Eve.
     *
     * <p>Options: {@link DayLength#FULL} means that the day is fully counted as work day, {@link DayLength#MORNING} and
     * {@link DayLength#NOON} means that only half of the day is counted as work day, {@link DayLength#ZERO} means that
     * the day is fully counted as public holiday</p>
     */
    @Enumerated(EnumType.STRING)
    private DayLength workingDurationForChristmasEve = DayLength.MORNING;

    @Enumerated(EnumType.STRING)
    private DayLength workingDurationForNewYearsEve = DayLength.MORNING;

    /**
     * Defines the federal state of Germany to be able to check correctly if a day is a public holiday or not.
     */
    @Enumerated(EnumType.STRING)
    private FederalState federalState = FederalState.BADEN_WUERTTEMBERG;

    private MailSettings mailSettings;

    private CalendarSettings calendarSettings;

    public DayLength getWorkingDurationForChristmasEve() {

        return workingDurationForChristmasEve;
    }


    public DayLength getWorkingDurationForNewYearsEve() {

        return workingDurationForNewYearsEve;
    }


    public FederalState getFederalState() {

        return federalState;
    }


    public void setWorkingDurationForChristmasEve(DayLength workingDurationForChristmasEve) {

        this.workingDurationForChristmasEve = workingDurationForChristmasEve;
    }


    public void setWorkingDurationForNewYearsEve(DayLength workingDurationForNewYearsEve) {

        this.workingDurationForNewYearsEve = workingDurationForNewYearsEve;
    }


    public void setFederalState(FederalState federalState) {

        this.federalState = federalState;
    }


    public AbsenceSettings getAbsenceSettings() {

        if (absenceSettings == null) {
            absenceSettings = new AbsenceSettings();
        }

        return absenceSettings;
    }


    public void setAbsenceSettings(AbsenceSettings absenceSettings) {

        this.absenceSettings = absenceSettings;
    }


    public MailSettings getMailSettings() {

        if (mailSettings == null) {
            mailSettings = new MailSettings();
        }

        return mailSettings;
    }


    public void setMailSettings(MailSettings mailSettings) {

        this.mailSettings = mailSettings;
    }


    public CalendarSettings getCalendarSettings() {

        if (calendarSettings == null) {
            calendarSettings = new CalendarSettings();
        }

        return calendarSettings;
    }


    public void setCalendarSettings(CalendarSettings calendarSettings) {

        this.calendarSettings = calendarSettings;
    }


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
