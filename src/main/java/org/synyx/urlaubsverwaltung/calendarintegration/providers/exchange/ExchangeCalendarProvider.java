package org.synyx.urlaubsverwaltung.calendarintegration.providers.exchange;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendCancellationsMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendInvitationsMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendInvitationsOrCancellationsMode;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.time.OlsonTimeZoneDefinition;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.Absence;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarMailService;
import org.synyx.urlaubsverwaltung.calendarintegration.CalendarNotCreatedException;
import org.synyx.urlaubsverwaltung.calendarintegration.providers.CalendarProvider;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.settings.CalendarSettings;
import org.synyx.urlaubsverwaltung.settings.ExchangeCalendarSettings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import java.util.TimeZone;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Date.from;
import static org.slf4j.LoggerFactory.getLogger;


/**
 * Provides sync of absences with exchange server calendar.
 */
@Deprecated(since = "4.0.0", forRemoval = true)
@Service
public class ExchangeCalendarProvider implements CalendarProvider {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    private final ExchangeService exchangeService;
    private final ExchangeFactory exchangeFactory;
    private final CalendarMailService calendarMailService;

    private String credentialsMailAddress;
    private String credentialsPassword;

    @Autowired
    public ExchangeCalendarProvider(CalendarMailService calendarMailService) {

        this(new ExchangeService(), new ExchangeFactory(), calendarMailService);
    }

    public ExchangeCalendarProvider(ExchangeService exchangeService, ExchangeFactory exchangeFactory, CalendarMailService calendarMailService) {

        this.exchangeService = exchangeService;
        this.exchangeFactory = exchangeFactory;
        this.calendarMailService = calendarMailService;
    }

    @Override
    public Optional<String> add(Absence absence, CalendarSettings calendarSettings) {

        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();
        String calendarName = exchangeCalendarSettings.getCalendar();
        connectToExchange(exchangeCalendarSettings);

        try {
            Appointment appointment = this.exchangeFactory.getNewAppointment(exchangeService);

            fillAppointment(absence, appointment, calendarSettings.getExchangeCalendarSettings().getTimeZoneId());

            SendInvitationsMode invitationsMode = SendInvitationsMode.SendToNone;

            if (exchangeCalendarSettings.isSendInvitationActive()) {
                invitationsMode = SendInvitationsMode.SendToAllAndSaveCopy;
            }

            if (calendarName.isEmpty()) {
                appointment.save(invitationsMode);
            } else {
                CalendarFolder calendarFolder = findOrCreateCalendar(calendarName);
                appointment.save(calendarFolder.getId(), invitationsMode);
            }

            LOG.info("Appointment {} for '{}' added to exchange calendar '{}'.", appointment.getId(),
                absence.getPerson().getId(), calendarName);

            return Optional.ofNullable(appointment.getId().getUniqueId());
        } catch (Exception ex) { // NOSONAR - EWS Java API throws Exception, that's life
            LOG.warn("An error occurred while trying to add appointment to Exchange calendar");
            calendarMailService.sendCalendarSyncErrorNotification(calendarName, absence, getStackTrace(ex));
        }

        return Optional.empty();
    }


    private void connectToExchange(ExchangeCalendarSettings settings) {

        String email = settings.getEmail();
        String password = settings.getPassword();

        String[] emailPart = email.split("[@._]");
        if (emailPart.length < 2) {
            LOG.warn("No connection could be established to the Exchange calendar for email={}, cause={}",
                email, "email-address is not valid (expected form: name@domain)");
            return;
        }
        String username = emailPart[0];
        String domain = emailPart[1];

        if (!email.equals(credentialsMailAddress) || !password.equals(credentialsPassword)) {
            try {
                exchangeService.setCredentials(new WebCredentials(username, password));
                exchangeService.setTraceEnabled(true);
                exchangeService.setEnableScpLookup(true);
                if (settings.getEwsUrl() == null) {
                    exchangeService.autodiscoverUrl(email, new RedirectionUrlCallback());
                } else {
                    exchangeService.setUrl(new URI(settings.getEwsUrl()));
                }
            } catch (Exception usernameException) { // NOSONAR - EWS Java API throws Exception, that's life
                LOG.info("No connection could be established to the Exchange calendar for username={}, cause={}",
                    username, usernameException.getMessage());
                try {
                    exchangeService.setCredentials(new WebCredentials(username, password, domain));
                    exchangeService.setTraceEnabled(true);
                    exchangeService.setEnableScpLookup(true);
                    exchangeService.autodiscoverUrl(email, new RedirectionUrlCallback());
                } catch (Exception usernameDomainException) { // NOSONAR - EWS Java API throws Exception, that's life
                    LOG.info("No connection could be established to the Exchange calendar for username={} and domain={}, cause={}",
                        username, domain, usernameDomainException.getMessage());

                    try {
                        exchangeService.setCredentials(new WebCredentials(email, password));
                        exchangeService.setTraceEnabled(true);
                        exchangeService.setEnableScpLookup(true);
                        exchangeService.autodiscoverUrl(email, new RedirectionUrlCallback());
                    } catch (Exception emailException) { // NOSONAR - EWS Java API throws Exception, that's life
                        LOG.warn("No connection could be established to the Exchange calendar for email={}, cause={}", email,
                            emailException.getMessage());
                    }
                }
            }

            credentialsMailAddress = email;
            credentialsPassword = password;
        }
    }


    private CalendarFolder findOrCreateCalendar(String calendarName) throws Exception { // NOSONAR - EWS Java API throws Exception, that's life

        Optional<CalendarFolder> calendarOptional = findCalendar(calendarName);

        if (calendarOptional.isPresent()) {
            return calendarOptional.get();
        } else {
            LOG.info("No exchange calendar found with name '{}'", calendarName);

            return createCalendar(calendarName);
        }
    }


    private Optional<CalendarFolder> findCalendar(String calendarName) throws Exception { // NOSONAR - EWS Java API throws Exception, that's life

        FindFoldersResults calendarRoot = exchangeService.findFolders(WellKnownFolderName.Calendar,
            new FolderView(Integer.MAX_VALUE));

        for (Folder folder : calendarRoot.getFolders()) {
            if (folder.getDisplayName().equals(calendarName)) {
                return Optional.of((CalendarFolder) folder);
            }
        }

        return Optional.empty();
    }


    private CalendarFolder createCalendar(String calendarName) {

        try {
            LOG.info("Trying to create new calendar with name '{}'", calendarName);

            CalendarFolder folder = new CalendarFolder(exchangeService);
            folder.setDisplayName(calendarName);
            folder.save(WellKnownFolderName.Calendar);

            LOG.info("New calendar folder '{}' created.", calendarName);

            return CalendarFolder.bind(exchangeService, folder.getId());
        } catch (Exception ex) { // NOSONAR - EWS Java API throws Exception, that's life
            throw new CalendarNotCreatedException(format("Exchange calendar '%s' could not be created",
                calendarName), ex);
        }
    }


    private void fillAppointment(Absence absence, Appointment appointment, String exchangeTimeZoneId) throws Exception { // NOSONAR - EWS Java API throws Exception, that's life

        Person person = absence.getPerson();

        appointment.setSubject(absence.getEventSubject());

        OlsonTimeZoneDefinition timeZone = new OlsonTimeZoneDefinition(TimeZone.getTimeZone(exchangeTimeZoneId));

        appointment.setStart(from(absence.getStartDate().toInstant()));
        appointment.setStartTimeZone(timeZone);
        appointment.setEnd(from(absence.getEndDate().toInstant()));
        appointment.setEndTimeZone(timeZone);

        appointment.setIsAllDayEvent(absence.isAllDay());
        appointment.getRequiredAttendees().add(person.getEmail());
        appointment.setIsReminderSet(false);
    }


    @Override
    public void update(Absence absence, String eventId, CalendarSettings calendarSettings) {

        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();
        String calendarName = exchangeCalendarSettings.getCalendar();
        connectToExchange(exchangeCalendarSettings);

        try {
            Appointment appointment = Appointment.bind(exchangeService, new ItemId(eventId));

            fillAppointment(absence, appointment, calendarSettings.getExchangeCalendarSettings().getTimeZoneId());

            SendInvitationsOrCancellationsMode notificationMode = SendInvitationsOrCancellationsMode.SendToNone;

            if (exchangeCalendarSettings.isSendInvitationActive()) {
                notificationMode = SendInvitationsOrCancellationsMode.SendToAllAndSaveCopy;
            }

            appointment.update(ConflictResolutionMode.AutoResolve, notificationMode);

            LOG.info("Appointment {} has been updated in exchange calendar '{}'.", eventId, calendarName);
        } catch (Exception ex) { // NOSONAR - EWS Java API throws Exception, that's life
            LOG.warn("Could not update appointment {} in exchange calendar '{}'", eventId, calendarName);
            calendarMailService.sendCalendarUpdateErrorNotification(calendarName, absence, eventId, getStackTrace(ex));
        }
    }


    @Override
    public void delete(String eventId, CalendarSettings calendarSettings) {

        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();
        String calendarName = exchangeCalendarSettings.getCalendar();
        connectToExchange(exchangeCalendarSettings);

        try {
            Appointment appointment = Appointment.bind(exchangeService, new ItemId(eventId));

            SendCancellationsMode notificationMode = SendCancellationsMode.SendToNone;

            if (exchangeCalendarSettings.isSendInvitationActive()) {
                notificationMode = SendCancellationsMode.SendToAllAndSaveCopy;
            }

            appointment.delete(DeleteMode.HardDelete, notificationMode);

            LOG.info("Appointment {} has been deleted in exchange calendar '{}'.", eventId, calendarName);
        } catch (Exception ex) { // NOSONAR - EWS Java API throws Exception, that's life
            LOG.warn("Could not delete appointment {} in exchange calendar '{}'", eventId, calendarName);
            calendarMailService.sendCalendarDeleteErrorNotification(calendarName, eventId, getStackTrace(ex));
        }
    }


    @Override
    public void checkCalendarSyncSettings(CalendarSettings calendarSettings) {

        ExchangeCalendarSettings exchangeCalendarSettings = calendarSettings.getExchangeCalendarSettings();
        connectToExchange(exchangeCalendarSettings);
    }

    private static class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {

        @Override
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {

            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }

    private static String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
