package org.synyx.urlaubsverwaltung.mail;

import net.fortuna.ical4j.model.Calendar;

import java.util.Objects;

public final class MailCalendarAttachment {

    private final String name;
    private final Calendar calendar;

    MailCalendarAttachment(String name, Calendar calendar) {
        this.calendar = calendar;
        this.name = name;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MailCalendarAttachment that = (MailCalendarAttachment) o;
        return Objects.equals(name, that.name) && Objects.equals(calendar, that.calendar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, calendar);
    }
}
