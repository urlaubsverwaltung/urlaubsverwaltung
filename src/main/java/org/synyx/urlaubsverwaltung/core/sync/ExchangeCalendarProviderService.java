package org.synyx.urlaubsverwaltung.core.sync;

import microsoft.exchange.webservices.data.autodiscover.IAutodiscoverRedirectionUrl;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendCancellationsMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendInvitationsMode;
import microsoft.exchange.webservices.data.core.service.folder.CalendarFolder;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FolderView;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Conditional;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sync.absence.Absence;
import org.synyx.urlaubsverwaltung.core.sync.condition.ExchangeCalendarCondition;

import java.util.Optional;


/**
 * Provides sync of absences with exchange server calendar.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service("calendarProviderService")
@Conditional(ExchangeCalendarCondition.class)
public class ExchangeCalendarProviderService implements CalendarProviderService {

    private static final Logger LOG = Logger.getLogger(ExchangeCalendarProviderService.class);

    private final String calendarName;
    private final MailService mailService;
    private ExchangeService exchangeService;

    @Autowired
    public ExchangeCalendarProviderService(MailService mailService,
        @Value("${calendar.ews.email}") String emailAddress,
        @Value("${calendar.ews.password}") String password,
        @Value("${calendar.ews.calendar}") String calendarName) {

        this.mailService = mailService;
        this.calendarName = calendarName;

        try {
            exchangeService = new ExchangeService();
            exchangeService.setCredentials(new WebCredentials(emailAddress, password));
            exchangeService.autodiscoverUrl(emailAddress, new RedirectionUrlCallback());
            exchangeService.setTraceEnabled(true);
        } catch (Exception e) {
            // NOTE: If an exception is thrown at this point, probably there is an error within the configuration

            LOG.error("No connection could be established to the Exchange calendar.");
            LOG.error("Please check your configuration!");
            LOG.error("Shutting down with system exit...");

            System.exit(1);
        }
    }

    @Override
    public Optional<String> addAbsence(Absence absence) {

        try {
            CalendarFolder calendarFolder = findOrCreateCalendar(calendarName);

            Appointment appointment = new Appointment(exchangeService);

            fillAppointment(absence, appointment);

            appointment.save(calendarFolder.getId(), SendInvitationsMode.SendToAllAndSaveCopy);

            LOG.info(String.format("Appointment %s for '%s' added to exchange calendar '%s'.", appointment.getId(),
                    absence.getPerson().getNiceName(), calendarFolder.getDisplayName()));

            return Optional.ofNullable(appointment.getId().getUniqueId());
        } catch (Exception ex) {
            LOG.warn("An error occurred while trying to add appointment to Exchange calendar");
            mailService.sendCalendarSyncErrorNotification(calendarName, absence, ex.getMessage());
        }

        return Optional.empty();
    }


    private CalendarFolder findOrCreateCalendar(String calendarName) {

        Optional<CalendarFolder> calendarOptional = findCalendar(calendarName);

        if (calendarOptional.isPresent()) {
            return calendarOptional.get();
        } else {
            return createCalendar(calendarName);
        }
    }


    private Optional<CalendarFolder> findCalendar(String calendarName) {

        try {
            FindFoldersResults calendarRoot = exchangeService.findFolders(WellKnownFolderName.Calendar,
                    new FolderView(Integer.MAX_VALUE));

            for (Folder folder : calendarRoot.getFolders()) {
                if (folder.getDisplayName().equals(calendarName)) {
                    return Optional.of((CalendarFolder) folder);
                }
            }
        } catch (Exception ex) {
            LOG.warn(String.format("No exchange calendar found with name '%s'", calendarName));
            throw new CalendarNotFoundException(String.format("No calendar found with name '%s'", calendarName), ex);
        }

        return Optional.empty();
    }


    private CalendarFolder createCalendar(String calendarName) {

        try {
            CalendarFolder folder = new CalendarFolder(exchangeService);
            folder.setDisplayName(calendarName);
            folder.save(WellKnownFolderName.Calendar);

            LOG.info(String.format("New calendar folder '%s' created.", calendarName));

            return CalendarFolder.bind(exchangeService, folder.getId());
        } catch (Exception ex) {
            LOG.warn(String.format("An error occurred during creation of exchange calendar with name '%s'",
                    calendarName));
            throw new CalendarNotCreatedException(String.format("Exchange calendar '%s' could not be created",
                    calendarName), ex);
        }
    }


    private void fillAppointment(Absence absence, Appointment appointment) throws Exception {

        Person person = absence.getPerson();

        appointment.setSubject(absence.getEventSubject());

        appointment.setStart(absence.getStartDate());
        appointment.setEnd(absence.getEndDate());
        appointment.getRequiredAttendees().add(person.getEmail());
    }


    @Override
    public void update(Absence absence, String eventId) {

        try {
            Appointment appointment = Appointment.bind(exchangeService, new ItemId(eventId));

            fillAppointment(absence, appointment);

            appointment.update(ConflictResolutionMode.AutoResolve);

            LOG.info(String.format("Appointment %s has been updated in exchange calendar '%s'.", eventId,
                    calendarName));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not update appointment %s in exchange calendar '%s'", eventId, calendarName));
            mailService.sendCalendarUpdateErrorNotification(calendarName, absence, eventId, ex.getMessage());
        }
    }


    @Override
    public void deleteAbsence(String eventId) {

        try {
            Appointment appointment = Appointment.bind(exchangeService, new ItemId(eventId));

            appointment.delete(DeleteMode.HardDelete, SendCancellationsMode.SendToAllAndSaveCopy);

            LOG.info(String.format("Appointment %s has been deleted in exchange calendar '%s'.", eventId,
                    calendarName));
        } catch (Exception ex) {
            LOG.warn(String.format("Could not delete appointment %s in exchange calendar '%s'", eventId, calendarName));
            mailService.sendCalendarDeleteErrorNotification(calendarName, eventId, ex.getMessage());
        }
    }

    private class RedirectionUrlCallback implements IAutodiscoverRedirectionUrl {

        @Override
        public boolean autodiscoverRedirectionUrlValidationCallback(String redirectionUrl) {

            return redirectionUrl.toLowerCase().startsWith("https://");
        }
    }
}
