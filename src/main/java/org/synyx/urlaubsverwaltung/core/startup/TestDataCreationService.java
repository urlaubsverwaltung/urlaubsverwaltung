package org.synyx.urlaubsverwaltung.core.startup;

import com.google.common.base.Optional;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationInteractionService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteInteractionService;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteType;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.math.BigDecimal;

import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class TestDataCreationService {

    private static final Logger LOG = Logger.getLogger(TestDataCreationService.class);

    private static final String ENVIRONMENT_PROPERTY = "env";
    private static final String DEV_ENVIRONMENT = "dev";

    private static final String USER = "test";

    @Autowired
    private PersonInteractionService personInteractionService;

    @Autowired
    private ApplicationInteractionService applicationInteractionService;

    @Autowired
    private SickNoteInteractionService sickNoteInteractionService;

    private Person user;
    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        String environment = System.getProperties().getProperty(ENVIRONMENT_PROPERTY);

        if (environment == null) {
            environment = DEV_ENVIRONMENT;
        }

        LOG.info("ENVIRONMENT=" + environment);

        if (environment.equals(DEV_ENVIRONMENT)) {
            LOG.info("Test data will be created...");

            user = createTestPerson(USER, "Marlene", "Muster", "mmuster@muster.de", Role.USER, Role.BOSS, Role.OFFICE);

            boss = createTestPerson("max", "Max", "Mustermann", "maxMuster@muster.de", Role.USER, Role.BOSS);

            office = createTestPerson("klaus", "Klaus", "Müller", "mueller@muster.de", Role.USER, Role.BOSS,
                    Role.OFFICE);

            createTestPerson("hdampf", "Hans", "Dampf", "dampf@foo.bar", Role.USER, Role.OFFICE);

            createTestPerson("horst", "Horst", "Dieter", "hdieter@muster.de", Role.INACTIVE);

            createTestData(user);
            createTestData(boss);
        } else {
            LOG.info("No test data will be created.");
        }
    }


    private Person createTestPerson(String login, String firstName, String lastName, String email, Role... roles)
        throws NoSuchAlgorithmException {

        int currentYear = DateMidnight.now().getYear();

        PersonForm personForm = new PersonForm(DateMidnight.now().getYear());
        personForm.setLoginName(login);
        personForm.setLastName(lastName);
        personForm.setFirstName(firstName);
        personForm.setEmail(email);

        personForm.setAnnualVacationDays(new BigDecimal("28.5"));
        personForm.setRemainingVacationDays(new BigDecimal("5"));
        personForm.setRemainingVacationDaysNotExpiring(BigDecimal.ZERO);
        personForm.setValidFrom(new DateMidnight(currentYear - 1, 1, 1));

        personForm.setWorkingDays(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(),
                Day.WEDNESDAY.getDayOfWeek(), Day.THURSDAY.getDayOfWeek(), Day.FRIDAY.getDayOfWeek()));

        personForm.setPermissions(Arrays.asList(roles));

        List<MailNotification> notifications = new ArrayList<>();

        notifications.add(MailNotification.NOTIFICATION_USER);

        if (personForm.getPermissions().contains(Role.BOSS)) {
            notifications.add(MailNotification.NOTIFICATION_BOSS);
        }

        if (personForm.getPermissions().contains(Role.OFFICE)) {
            notifications.add(MailNotification.NOTIFICATION_OFFICE);
        }

        personForm.setNotifications(notifications);

        return personInteractionService.create(personForm);
    }


    private void createTestData(Person person) {

        DateMidnight now = DateMidnight.now();

        // FUTURE APPLICATIONS FOR LEAVE
        createWaitingApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(10), now.plusDays(16)); // NOSONAR
        createWaitingApplication(person, VacationType.OVERTIME, DayLength.FULL, now.plusDays(1), now.plusDays(2)); // NOSONAR
        createWaitingApplication(person, VacationType.SPECIALLEAVE, DayLength.FULL, now.plusDays(4), now.plusDays(6)); // NOSONAR

        // PAST APPLICATIONS FOR LEAVE
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(20), now.minusDays(13)); // NOSONAR
        createAllowedApplication(person, VacationType.HOLIDAY, DayLength.MORNING, now.minusDays(5), now.minusDays(5)); // NOSONAR
        createAllowedApplication(person, VacationType.SPECIALLEAVE, DayLength.MORNING, now.minusDays(9), // NOSONAR
            now.minusDays(9)); // NOSONAR

        createRejectedApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(33), now.minusDays(30)); // NOSONAR

        createCancelledApplication(person, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(11), now.minusDays(10)); // NOSONAR

        // SICK NOTES
        createSickNote(person, now.minusDays(10), now.minusDays(10), SickNoteType.SICK_NOTE, false); // NOSONAR
        createSickNote(person, now.minusDays(30), now.minusDays(25), SickNoteType.SICK_NOTE, true); // NOSONAR
        createSickNote(person, now.minusDays(60), now.minusDays(55), SickNoteType.SICK_NOTE_CHILD, true); // NOSONAR
        createSickNote(person, now.minusDays(44), now.minusDays(44), SickNoteType.SICK_NOTE_CHILD, false); // NOSONAR
    }


    private Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)) {
            application = new Application();
            application.setPerson(person);
            application.setStartDate(startDate);
            application.setEndDate(endDate);
            application.setVacationType(vacationType);
            application.setHowLong(dayLength);

            applicationInteractionService.apply(application, person, Optional.of("Ich hätte gerne Urlaub"));
        }

        return application;
    }


    private boolean startAndEndDatesAreInCurrentYear(DateMidnight start, DateMidnight end) {

        int currentYear = DateMidnight.now().getYear();

        return start.getYear() == currentYear && end.getYear() == currentYear;
    }


    private Application createAllowedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.allow(application, boss, Optional.of("Ist in Ordnung"));
        }

        return application;
    }


    private Application createRejectedApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.reject(application, boss,
                Optional.of("Aus organisatorischen Gründen leider nicht möglich"));
        }

        return application;
    }


    private Application createCancelledApplication(Person person, VacationType vacationType, DayLength dayLength,
        DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, vacationType, dayLength, startDate, endDate);

        if (application != null) {
            applicationInteractionService.cancel(application, office,
                Optional.of("Urlaub wurde nicht genommen, daher storniert"));
        }

        return application;
    }


    private SickNote createSickNote(Person person, DateMidnight startDate, DateMidnight endDate, SickNoteType type,
        boolean withAUB) {

        SickNote sickNote = null;

        if (startAndEndDatesAreInCurrentYear(startDate, endDate)) {
            sickNote = new SickNote();
            sickNote.setPerson(person);
            sickNote.setStartDate(startDate);
            sickNote.setEndDate(endDate);
            sickNote.setActive(true);
            sickNote.setType(type);

            if (withAUB) {
                sickNote.setAubStartDate(startDate);
                sickNote.setAubEndDate(endDate);
            }

            sickNoteInteractionService.create(sickNote, office);
        }

        return sickNote;
    }
}
