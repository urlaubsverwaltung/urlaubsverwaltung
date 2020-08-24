package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendar.config.ICalProperties;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Date.from;
import static java.util.stream.Collectors.toList;
import static net.fortuna.ical4j.model.property.CalScale.GREGORIAN;
import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;


@Service
class ICalService {

    private final ICalProperties iCalProperties;

    public ICalService(ICalProperties iCalProperties) {
        this.iCalProperties = iCalProperties;
    }

    String generateCalendar(String title, List<Absence> absences) {

        final Calendar calendar = new Calendar();
        calendar.getProperties().add(VERSION_2_0);
        calendar.getProperties().add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.getProperties().add(GREGORIAN);
        calendar.getProperties().add(new XProperty("X-WR-CALNAME", title));
        calendar.getProperties().add(new XProperty("X-MICROSOFT-CALSCALE", GREGORIAN.getValue()));

        final List<VEvent> absencesVEvents = absences.stream()
            .filter(notOlderThan(iCalProperties.getDaysInPast()))
            .map(this::toVEvent).collect(toList());
        calendar.getComponents().addAll(absencesVEvents);

        final StringWriter calenderWriter = new StringWriter();
        final CalendarOutputter calendarOutputter = new CalendarOutputter();
        try {
            calendarOutputter.output(calendar, calenderWriter);
        } catch (ValidationException | IOException e) {
            throw new CalendarException("iCal calender could not be generated", e);
        }

        return calenderWriter.toString();
    }

    private Predicate<? super Absence> notOlderThan(Integer days) {
        return (absence ->
        {
            // Adding +1L is needed to be able to use .isAfter() method
            var filterDate = LocalDate.now(ZoneOffset.UTC).minusDays(days + 1L);
            return absence.getStartDate().toLocalDate().isAfter(filterDate)
                || absence.getEndDate().toLocalDate().isAfter(filterDate);
        });
    }


    private VEvent toVEvent(Absence absence) {


        final ZonedDateTime startDateTime = absence.getStartDate();
        final ZonedDateTime endDateTime = absence.getEndDate();

        final DateTime start = new DateTime(from(startDateTime.toInstant()));
        start.setUtc(true);
        final DateTime end = new DateTime(from(endDateTime.toInstant()));
        end.setUtc(true);

        final VEvent event;
        if (absence.isAllDay()) {
            if (isSameDay(startDateTime, endDateTime)) {
                event = new VEvent(new Date(start.getTime()), absence.getEventSubject());
            } else {
                event = new VEvent(new Date(start), new Date(end), absence.getEventSubject());
            }

            event.getProperties().add(new XProperty("X-MICROSOFT-CDO-ALLDAYEVENT", "TRUE"));
        } else {
            event = new VEvent(start, end, absence.getEventSubject());
        }

        event.getProperties().add(new Uid(generateUid(absence)));

        return event;
    }

    private boolean isSameDay(ZonedDateTime startDateTime, ZonedDateTime endDate) {
        return startDateTime.toLocalDate().isEqual(endDate.toLocalDate().minusDays(1));
    }

    private String generateUid(Absence absence) {
        final String data = absence.getStartDate() + "" + absence.getEndDate() + "" + absence.getPerson();
        return DigestUtils.md5Hex(data).toUpperCase();
    }
}
