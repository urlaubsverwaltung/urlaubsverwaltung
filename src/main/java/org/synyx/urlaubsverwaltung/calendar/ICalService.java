package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.Date.from;
import static java.util.stream.Collectors.toList;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;


@Service
class ICalService {

    String generateCalendar(String title, List<Absence> absences) {

        final Calendar calendar = new Calendar();
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", title));

        final List<VEvent> absencesVEvents = absences.stream().map(this::toVEvent).collect(toList());
        calendar.getComponents().addAll(absencesVEvents);

        try {
            calendar.validate();
        } catch (ValidationException e) {
            throw new CalendarException("Validation does not pass", e);
        }

        return calendar.toString();
    }


    private VEvent toVEvent(Absence absence) {

        final VTimeZone utc = TimeZoneRegistryFactory.getInstance().createRegistry()
            .getTimeZone("Etc/UTC").getVTimeZone();

        final Instant startDateTime = absence.getStartDate();
        final Instant endDateTime = absence.getEndDate();

        final TimeZone timeZone = new TimeZone(utc);
        final DateTime start = new DateTime(from(startDateTime), timeZone);
        final DateTime end = new DateTime(from(endDateTime), timeZone);

        final VEvent event;
        if (absence.isAllDay() && isSameDay(startDateTime, endDateTime)) {
            event = new VEvent(new Date(start.getTime()), absence.getEventSubject());
        } else if (absence.isAllDay() && !isSameDay(startDateTime, endDateTime)) {
            event = new VEvent(new Date(start), new Date(end), absence.getEventSubject());
        } else {
            event = new VEvent(start, end, absence.getEventSubject());
        }

        event.getProperties().add(new Uid(generateUid(absence)));

        return event;
    }

    private boolean isSameDay(Instant startDateTime, Instant endDate) {
        return startDateTime.equals(endDate.minus(1, ChronoUnit.DAYS));
    }

    private String generateUid(Absence absence) {
        final String data = absence.getStartDate() + "" + absence.getEndDate() + "" + absence.getPerson();
        return DigestUtils.md5Hex(data).toUpperCase();
    }
}
