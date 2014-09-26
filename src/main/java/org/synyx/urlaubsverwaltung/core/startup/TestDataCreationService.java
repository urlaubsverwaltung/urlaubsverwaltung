package org.synyx.urlaubsverwaltung.core.startup;

import org.apache.log4j.Logger;
import org.joda.time.DateMidnight;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.core.account.Account;
import org.synyx.urlaubsverwaltung.core.account.AccountDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.Comment;
import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.application.domain.VacationType;
import org.synyx.urlaubsverwaltung.core.application.service.ApplicationService;
import org.synyx.urlaubsverwaltung.core.application.service.CommentService;
import org.synyx.urlaubsverwaltung.core.calendar.Day;
import org.synyx.urlaubsverwaltung.core.calendar.workingtime.WorkingTimeService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonDAO;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.core.sicknote.comment.SickNoteStatus;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;
import org.synyx.urlaubsverwaltung.security.Role;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * @author Aljona Murygina - murygina@synyx.de
 */
@Service
public class TestDataCreationService {

    private static final Logger LOG = Logger.getLogger(TestDataCreationService.class);

    private static final String IN_MEMORY_DATABASE = "h2";
    private static final String USER = "test";

    private static final Boolean ACTIVE = true;
    private static final Boolean INACTIVE = false;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private AccountDAO accountDAO;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private WorkingTimeService workingTimeService;

    @Autowired
    private SickNoteService sickNoteService;

    @Autowired
    private CommentService commentService;

    private Person user;
    private Person boss;
    private Person office;

    @PostConstruct
    public void createTestData() throws NoSuchAlgorithmException {

        String dbType = System.getProperties().getProperty("db");

        LOG.info("Using database type = " + dbType);


        if (dbType.equals(IN_MEMORY_DATABASE)) {
            LOG.info("Test data will be created...");

            user = createTestPerson(USER, "Marlene", "Muster", "mmuster@muster.de", ACTIVE, Role.USER, Role.BOSS, Role.OFFICE);
            boss = createTestPerson("max", "Max", "Mustermann", "maxMuster@muster.de", ACTIVE, Role.USER, Role.BOSS);
            office = createTestPerson("klaus", "Klaus", "Müller", "müller@muster.de", ACTIVE, Role.USER, Role.BOSS, Role.OFFICE);
            Person inactivePerson = createTestPerson("horst", "Horst", "Dieter", "hdieter@muster.de", INACTIVE, Role.INACTIVE);

            personDAO.save(user);
            personDAO.save(boss);
            personDAO.save(office);
            personDAO.save(inactivePerson);

            accountDAO.save(createTestAccount(user, true));
            accountDAO.save(createTestAccount(boss, false));
            accountDAO.save(createTestAccount(office, true));
            accountDAO.save(createTestAccount(inactivePerson, true));

            createTestWorkingTime(user);

            DateMidnight now = DateMidnight.now();

            createWaitingApplication(user, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(10), now.minusDays(16));
            createWaitingApplication(user, VacationType.OVERTIME, DayLength.FULL, now.plusDays(1), now.minusDays(2));
            createWaitingApplication(user, VacationType.SPECIALLEAVE, DayLength.FULL, now.plusDays(1), now.minusDays(2));

            createAllowedApplication(user, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(20), now.minusDays(13));
            createAllowedApplication(user, VacationType.HOLIDAY, DayLength.MORNING, now.minusDays(5), now.minusDays(5));

            createRejectedApplication(user, VacationType.HOLIDAY, DayLength.FULL, now.plusDays(30), now.plusDays(33));

            createCancelledApplication(user, VacationType.HOLIDAY, DayLength.FULL, now.minusDays(10), now.minusDays(11));

            createSickNote(user, now.minusDays(10), now.minusDays(10));
            createSickNote(user, now.minusDays(30), now.minusDays(25));

        } else {
            LOG.info("No test data is created.");
        }


    }

    private Person createTestPerson(String login, String firstName, String lastName, String email, boolean active, Role... roles) throws NoSuchAlgorithmException {

        Person person = new Person(login, firstName, lastName, email);
        person.setActive(active);
        person.setPermissions(Arrays.asList(roles));

        KeyPair keyPair = CryptoUtil.generateKeyPair();

        person.setPrivateKey(keyPair.getPrivate().getEncoded());
        person.setPublicKey(keyPair.getPublic().getEncoded());

        return person;

    }

    private Account createTestAccount(Person person, boolean remainingVacationDaysExpire) {

        int year = DateMidnight.now().getYear();

        DateMidnight firstDayOfYear = new DateMidnight(year, 1, 1);
        DateMidnight lastDayOfYear = new DateMidnight(year, 12, 31);

        Account account = new Account(person, firstDayOfYear.toDate(), lastDayOfYear.toDate(), new BigDecimal("28"), new BigDecimal("5"), remainingVacationDaysExpire);

        account.setVacationDays(new BigDecimal("28"));

        return account;

    }

    private void createTestWorkingTime(Person person) {

        workingTimeService.touch(Arrays.asList(Day.MONDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(), Day.WEDNESDAY.getDayOfWeek(), Day.TUESDAY.getDayOfWeek(), Day.FRIDAY.getDayOfWeek()), new DateMidnight(DateMidnight.now().getYear(), 1, 1), person);

    }

    private Application createWaitingApplication(Person person, VacationType vacationType, DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        Application application = new Application();
        application.setPerson(person);
        application.setStartDate(startDate);
        application.setEndDate(endDate);
        application.setVacationType(vacationType);
        application.setHowLong(dayLength);

        Application waitingApplication = applicationService.apply(application, person, person);

        applicationService.save(waitingApplication);

        // TODO: comment should be generated and saved in application service
        commentService.saveComment(new Comment(), person, application);

        return waitingApplication;

    }

    private Application createAllowedApplication(Person person, VacationType vacationType, DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Ist ok");

        applicationService.allow(application, boss, comment);

        // TODO: comment should be generated and saved in application service
        commentService.saveComment(comment, boss, application);

        return application;

    }

    private Application createRejectedApplication(Person person, VacationType vacationType, DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        Application application = createWaitingApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Leider nicht möglich");

        applicationService.reject(application, boss);

        // TODO: comment should be generated and saved in application service
        commentService.saveComment(comment, boss, application);

        return application;

    }

    private Application createCancelledApplication(Person person, VacationType vacationType, DayLength dayLength, DateMidnight startDate, DateMidnight endDate) {

        Application application = createAllowedApplication(person, vacationType, dayLength, startDate, endDate);

        Comment comment = new Comment();
        comment.setReason("Urlaub wurde doch nicht genommen");

        applicationService.cancel(application);

        // TODO: comment should be generated and saved in application service
        commentService.saveComment(comment, user, application);

        return application;

    }

    private SickNote createSickNote(Person person, DateMidnight startDate, DateMidnight endDate) {

        SickNote sickNote = new SickNote();
        sickNote.setPerson(person);
        sickNote.setStartDate(startDate);
        sickNote.setEndDate(endDate);

        sickNoteService.touch(sickNote, SickNoteStatus.CREATED, office);

        return sickNote;

    }
}
