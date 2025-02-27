package org.synyx.urlaubsverwaltung.calendar;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RefreshInterval;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.validate.ValidationException;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.user.UserSettingsService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static net.fortuna.ical4j.model.parameter.Role.REQ_PARTICIPANT;
import static net.fortuna.ical4j.model.property.immutable.ImmutableCalScale.GREGORIAN;
import static net.fortuna.ical4j.model.property.immutable.ImmutableMethod.CANCEL;
import static net.fortuna.ical4j.model.property.immutable.ImmutableTransp.TRANSPARENT;
import static net.fortuna.ical4j.model.property.immutable.ImmutableVersion.VERSION_2_0;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.CANCELLED;
import static org.synyx.urlaubsverwaltung.calendar.ICalType.PUBLISHED;


@Service
public class ICalService {

    private final CalendarProperties calendarProperties;
    private final MessageSource messageSource;
    private final UserSettingsService userSettingsService;

    @Autowired
    ICalService(
        CalendarProperties calendarProperties,
        MessageSource messageSource,
        UserSettingsService userSettingsService
    ) {
        this.calendarProperties = calendarProperties;
        this.messageSource = messageSource;
        this.userSettingsService = userSettingsService;
    }

    public ByteArrayResource getCalendar(String title, List<CalendarAbsence> absences, Person recipient) {
        final Calendar calendar = generateCalendar(title, absences, recipient);
        return writeCalenderIntoRessource(calendar);
    }

    public ByteArrayResource getSingleAppointment(CalendarAbsence absence, ICalType method, Person recipient) {
        final Calendar calendar = generateForSingleAppointment(absence, method, recipient);
        return writeCalenderIntoRessource(calendar);
    }

    private Calendar generateCalendar(String title, List<CalendarAbsence> absences, Person recipient) {
        final Calendar calendar = prepareCalendar(absences, PUBLISHED, recipient);
        calendar.add(new XProperty("X-WR-CALNAME", title));
        calendar.add(new RefreshInterval(new ParameterList(), calendarProperties.getRefreshInterval()));
        return calendar;
    }

    private Calendar generateForSingleAppointment(CalendarAbsence absence, ICalType method, Person recipient) {
        return prepareCalendar(List.of(absence), method, recipient);
    }

    private Calendar prepareCalendar(List<CalendarAbsence> absences, ICalType method, Person recipient) {

        final Locale locale = userSettingsService.getEffectiveLocale(List.of(recipient)).get(recipient);

        final Calendar calendar = new Calendar();
        calendar.add(VERSION_2_0);
        calendar.add(new ProdId("-//Urlaubsverwaltung//iCal4j 1.0//DE"));
        calendar.add(GREGORIAN);
        calendar.add(new XProperty("X-MICROSOFT-CALSCALE", GREGORIAN.getValue()));

        if (method == CANCELLED) {
            calendar.add(CANCEL);
        }

        absences.stream()
            .map(absence -> this.toVEvent(absence, method, absence.getPerson().equals(recipient), locale))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(calendar::add);

        return calendar;
    }

    private Optional<VEvent> toVEvent(CalendarAbsence absence, ICalType method, boolean isOwn, Locale locale) {

        final ZonedDateTime startDateTime = absence.getStartDate();
        final ZonedDateTime endDateTime = absence.getEndDate();
        final String summary = getTranslation(locale, absence.getCalendarAbsenceTypeMessageKey(), absence.getPerson().getNiceName());

        final VEvent event;
        if (absence.isAllDay()) {
            if (isSameDay(startDateTime, endDateTime)) {
                event = new VEvent(startDateTime.toLocalDate(), summary);
            } else {
                event = new VEvent(startDateTime.toLocalDate(), endDateTime.toLocalDate(), summary);
            }
            event.add(new XProperty("X-MICROSOFT-CDO-ALLDAYEVENT", "TRUE"));
        } else {
            event = new VEvent(startDateTime.toInstant(), endDateTime.toInstant(), summary);
        }

        event.add(new Uid(generateUid(absence)));
        if (absence.getPerson().getEmail() != null) {
            event.add(generateAttendee(absence));
        }

        if (absence.isHolidayReplacement() || !isOwn) {
            event.add(TRANSPARENT);
        }

        if (method == CANCELLED) {
            event.add(new Sequence(1));
        }

        event.add(new Organizer(URI.create("mailto:" + calendarProperties.getOrganizer())));

        return Optional.of(event);
    }

    private Attendee generateAttendee(CalendarAbsence absence) {
        final Attendee attendee = new Attendee(URI.create("mailto:" + absence.getPerson().getEmail()));
        attendee.add(REQ_PARTICIPANT);
        attendee.add(new Cn(absence.getPerson().getNiceName()));

        return attendee;
    }

    private boolean isSameDay(ZonedDateTime startDateTime, ZonedDateTime endDate) {
        return startDateTime.toLocalDate().isEqual(endDate.toLocalDate().minusDays(1));
    }

    private String generateUid(CalendarAbsence absence) {
        final String data = absence.getStartDate() + "" + absence.getEndDate() + absence.getPerson();
        return DigestUtils.md5Hex(data).toUpperCase();
    }

    private ByteArrayResource writeCalenderIntoRessource(Calendar calendar) {

        final boolean validation = !calendar.getComponents().isEmpty();

        final ByteArrayResource byteArrayResource;
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final CalendarOutputter calendarOutputter = new CalendarOutputter(validation);
            calendarOutputter.output(calendar, byteArrayOutputStream);
            byteArrayResource = new ByteArrayResource(byteArrayOutputStream.toByteArray());
        } catch (ValidationException | IOException e) {
            throw new CalendarException("iCal calendar could not be written to ByteArrayResource", e);
        }
        return byteArrayResource;
    }

    private String getTranslation(Locale locale, String key, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
