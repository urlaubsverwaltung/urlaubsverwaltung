package org.synyx.urlaubsverwaltung.core.sync.absence;

import com.google.common.base.MoreObjects;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Date;


/**
 * Represents a period of time where a person is not at work.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class Absence {

    private static final int HOURS_IN_MILLISECONDS = 60 * 60 * 1000;
    private static final int NOON_START = 13 * HOURS_IN_MILLISECONDS;
    private static final int NOON_END = 17 * HOURS_IN_MILLISECONDS;
    private static final int MORNING_END = 12 * HOURS_IN_MILLISECONDS;
    private static final int MORNING_START = 8 * HOURS_IN_MILLISECONDS;

    private Date startDate;

    private Date endDate;

    private Person person;

    private boolean isAllDay = false;

    public Absence(Application application) {

        Assert.notNull(application.getHowLong(), "No day length set for application");

        this.person = application.getPerson();

        Date applicationStart = application.getStartDate().toDate();
        Date applicationEnd = application.getEndDate().toDate();

        switch (application.getHowLong()) {
            case FULL:
                this.startDate = applicationStart;
                this.endDate = applicationEnd;
                this.isAllDay = true;
                break;

            case MORNING:
                this.startDate = new Date(applicationStart.getTime() + MORNING_START);
                this.endDate = new Date(applicationEnd.getTime() + MORNING_END);
                break;

            case NOON:
                this.startDate = new Date(applicationStart.getTime() + NOON_START);
                this.endDate = new Date(applicationEnd.getTime() + NOON_END);
                break;

            default:
                throw new IllegalArgumentException("Invalid day length for application!");
        }
    }


    public Absence(SickNote sickNote) {

        this.startDate = sickNote.getStartDate().toDate();
        this.endDate = sickNote.getEndDate().toDate();
        this.person = sickNote.getPerson();

        // TODO: at the moment sick notes have no day length
        this.isAllDay = true;
    }

    public Date getStartDate() {

        return startDate;
    }


    public Date getEndDate() {

        return endDate;
    }


    public Person getPerson() {

        return person;
    }


    public boolean isAllDay() {

        return isAllDay;
    }


    @Override
    public String toString() {

        MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);

        toStringHelper.add("person", getPerson().getLoginName());
        toStringHelper.add("startDate", getStartDate());
        toStringHelper.add("endDate", getEndDate());
        toStringHelper.add("isAllDay", isAllDay());

        return toStringHelper.toString();
    }
}
