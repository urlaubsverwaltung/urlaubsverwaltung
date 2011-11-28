package org.synyx.urlaubsverwaltung.service;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.transaction.annotation.Transactional;

import org.synyx.urlaubsverwaltung.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Comment;
import org.synyx.urlaubsverwaltung.domain.HolidayEntitlement;
import org.synyx.urlaubsverwaltung.domain.HolidaysAccount;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.math.BigDecimal;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.List;


/**
 * implementation of the applicationdata-access-service.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private static final int LAST_DAY = 31;
    private static final int FIRST_DAY = 1;

    private ApplicationDAO applicationDAO;
    private HolidaysAccountService accountService;
    private CryptoService cryptoService;
    private OwnCalendarService calendarService;
    private MailService mailService;
    private CalculationService calculationService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, HolidaysAccountService accountService,
        CryptoService cryptoService, OwnCalendarService calendarService, MailService mailService,
        CalculationService calculationService) {

        this.applicationDAO = applicationDAO;
        this.accountService = accountService;
        this.cryptoService = cryptoService;
        this.calendarService = calendarService;
        this.mailService = mailService;
        this.calculationService = calculationService;
    }

    /**
     * @see  ApplicationService#getAllApplicationsForPerson(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<Application> getAllApplicationsForPerson(Person person) {

        return applicationDAO.getAllApplicationsForPerson(person);
    }


    /**
     * @see  ApplicationService#getAllApplicationByState(org.synyx.urlaubsverwaltung.domain.ApplicationStatus)
     */
    @Override
    public List<Application> getAllApplicationByState(ApplicationStatus state) {

        return applicationDAO.getAllApplicationsByState(state);
    }


    /**
     * @see  ApplicationService#getAllApplicationsForACertainTime(org.joda.time.DateMidnight, org.joda.time.DateMidnight)
     */
    @Override
    public List<Application> getAllApplicationsForACertainTime(DateMidnight startDate, DateMidnight endDate) {

        return applicationDAO.getAllApplicationsForACertainTime(startDate, endDate);
    }


    /**
     * @see  ApplicationService#wait(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void wait(Application application) {

        application.setStatus(ApplicationStatus.WAITING);
        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#allow(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void allow(Application application) {

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // set number of used days
        application.setDays(calendarService.getVacationDays(application, application.getStartDate(),
                application.getEndDate()));

        applicationDAO.save(application);

        mailService.sendApprovedNotification(application.getPerson(), application);
    }


    /**
     * @see  ApplicationService#save(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void save(Application application) {

        // if check successful, application is saved

        Person person = application.getPerson();
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        // start date and end date: same year?
        if (start.getYear() == end.getYear()) {
            HolidaysAccount account = accountService.getHolidaysAccount(start.getYear(), person);

            // if account not yet existent, create one
            if (account == null) {
                account = accountService.newHolidaysAccount(person,
                        accountService.getHolidayEntitlement(start.getYear(), person).getVacationDays(),
                        BigDecimal.ZERO, start.getYear());
            }

            // notice special case april
            calculationService.noticeApril(application, account);

            // then save
            accountService.saveHolidaysAccount(account);
        } else {
            // if start.getYear() != end.getYear() there are two accounts

            HolidaysAccount accountCurrentYear = accountService.getHolidaysAccount(start.getYear(),
                    application.getPerson());
            HolidaysAccount accountNextYear = accountService.getHolidaysAccount(end.getYear(), application.getPerson());

            // notice special case January
            calculationService.noticeJanuary(application, accountCurrentYear, accountNextYear);

            // then save both accounts
            accountService.saveHolidaysAccount(accountCurrentYear);
            accountService.saveHolidaysAccount(accountNextYear);
        }

        // save changed application in the end
        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#reject(org.synyx.urlaubsverwaltung.domain.Application,org.synyx.urlaubsverwaltung.domain.Person,
     *       java.lang.String)
     */
    @Override
    public void reject(Application application, Person boss, String reasonToReject) {

        application.setStatus(ApplicationStatus.REJECTED);

        application.setBoss(boss);

        Comment comment = new Comment();
        comment.setText(reasonToReject);
        comment.setPerson(boss);
        comment.setDateOfComment(new DateMidnight(DateMidnight.now().getYear(), DateMidnight.now().getMonthOfYear(),
                DateMidnight.now().getDayOfMonth()));

        application.setReasonToReject(comment);

        accountService.rollbackUrlaub(application);

        applicationDAO.save(application);

        mailService.sendDeclinedNotification(application);
    }


    /**
     * @see  ApplicationService#cancel(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void cancel(Application application) {

        accountService.rollbackUrlaub(application);

        if (application.getStatus() == ApplicationStatus.WAITING) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been waiting, chefs get email
            mailService.sendCanceledNotification(application, EmailAdr.CHEFS.getEmail());
        } else if (application.getStatus() == ApplicationStatus.ALLOWED) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been allowed, office gets email
            mailService.sendCanceledNotification(application, EmailAdr.OFFICE.getEmail());
        }
    }


    /**
     * @see  ApplicationService#addSickDaysOnHolidaysAccount(org.synyx.urlaubsverwaltung.domain.Application, double)
     */
    @Override
    public void addSickDaysOnHolidaysAccount(Application application, double sickDays) {

        application.setSickDays(BigDecimal.valueOf(sickDays));
        application.setDays(application.getDays().subtract(BigDecimal.valueOf(sickDays)));

        Person person = application.getPerson();
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        if (start.getYear() != end.getYear()) {
            HolidaysAccount account = accountService.getHolidaysAccount(end.getYear(), person);

            // if account not yet existing, create one
            if (account == null) {
                account = accountService.newHolidaysAccount(person,
                        accountService.getHolidayEntitlement(end.getYear(), person).getVacationDays(), BigDecimal.ZERO,
                        end.getYear());
            }

            account.setVacationDays(account.getVacationDays().add(BigDecimal.valueOf(sickDays)));

            // compareTo returns -1, 0, or 1 as Number1 is numerically
            // less than, equal to, or greater Number2
            if (account.getVacationDays().compareTo(
                        accountService.getHolidayEntitlement(end.getYear(), person).getVacationDays()) == 1) {
                account.setRemainingVacationDays((account.getRemainingVacationDays().add(account.getVacationDays()))
                    .subtract(accountService.getHolidayEntitlement(end.getYear(), person).getVacationDays()));
                account.setVacationDays(accountService.getHolidayEntitlement(end.getYear(), person).getVacationDays());
            }
        } else {
            HolidaysAccount account = accountService.getHolidaysAccount(start.getYear(), person);
            HolidayEntitlement entitlement = accountService.getHolidayEntitlement(start.getYear(), person);

            BigDecimal newVacDays = BigDecimal.valueOf(sickDays).add(account.getVacationDays());

            if (newVacDays.compareTo(entitlement.getVacationDays()) == 1) {
                // if it is not yet April, vacation days and remaining vacation days are filled
                if (application.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
                    account.setRemainingVacationDays(newVacDays.subtract(entitlement.getVacationDays()));
                    newVacDays = entitlement.getVacationDays();
                }

                // if April is over, only vacation days are filled, remaining vacation days are not filled after April
                // because after April there exists no remaining leave
                if (application.getEndDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
                    newVacDays = entitlement.getVacationDays();
                }
            }

            account.setVacationDays(newVacDays);
        }

        applicationDAO.save(application);
    }


    /**
     * @see  ApplicationService#signApplicationByBoss(org.synyx.urlaubsverwaltung.domain.Application, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void signApplicationByBoss(Application application, Person boss) {

        byte[] data = signAntrag(application, boss);

        if (data != null) {
            application.setSignatureBoss(data);
            applicationDAO.save(application);
        }
    }


    /**
     * @see  ApplicationService#signApplicationByUser(org.synyx.urlaubsverwaltung.domain.Application, org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public void signApplicationByUser(Application application, Person user) {

        byte[] data = signAntrag(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }


    // exceptions has to be catched!
    public byte[] signAntrag(Application application, Person person) {

        try {
            PrivateKey privKey = cryptoService.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = cryptoService.sign(privKey, data);

            return data;
        } catch (InvalidKeyException ex) {
        } catch (SignatureException ex) {
        } catch (NoSuchAlgorithmException ex) {
        } catch (InvalidKeySpecException ex) {
        }

        return null;
    }


    /**
     * @see  ApplicationService#checkAntrag(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public boolean checkAntrag(Application application) {

        if (application.getStartDate().getYear() == application.getEndDate().getYear()) {
            return checkAntragOneYear(application);
        } else {
            return checkAntragTwoYears(application);
        }
    }


    public boolean checkAntragOneYear(Application application) {

        BigDecimal days;
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();
        Person person = application.getPerson();

        HolidaysAccount account = accountService.getHolidaysAccount(start.getYear(), person);

        // if account not yet existent
        if (account == null) {
            // create new account
            account = accountService.newHolidaysAccount(person,
                    accountService.getHolidayEntitlement(start.getYear(), person).getVacationDays(), BigDecimal.ZERO,
                    start.getYear());
        }

        // if application is before April
        if (application.getEndDate().getMonthOfYear() < DateTimeConstants.APRIL) {
            days = calendarService.getVacationDays(application, start, end);

            // compareTo >= 0 means: greater than or equal (because it may be 0 or 1)
            if (((account.getRemainingVacationDays().add(account.getVacationDays())).subtract(days)).compareTo(
                        BigDecimal.valueOf(0.0)) >= 0) {
                return true;
            }
        } else if (application.getStartDate().getMonthOfYear() >= DateTimeConstants.APRIL) {
            // if application is after April, there are no remaining vacation days

            days = calendarService.getVacationDays(application, start, end);

            // compareTo >= 0 means: greater than or equal (because it may be 0 or 1)
            if ((account.getVacationDays().subtract(days)).compareTo(BigDecimal.valueOf(0.0)) >= 0) {
                return true;
            }
        } else {
            // if period of holiday is before AND after april
            BigDecimal beforeApril = calendarService.getVacationDays(application, application.getStartDate(),
                    new DateMidnight(start.getYear(), DateTimeConstants.MARCH, LAST_DAY));
            BigDecimal afterApril = calendarService.getVacationDays(application,
                    new DateMidnight(end.getYear(), DateTimeConstants.APRIL, FIRST_DAY), application.getEndDate());

            // subtract beforeApril from remaining vacation days
            BigDecimal result = account.getRemainingVacationDays().subtract(beforeApril);

            // if beforeApril is greater than remaining vacation days
            // (remaining vacation days minus beforeApril) has to be subtracted from vacation days
            if (result.compareTo(BigDecimal.ZERO) == 1) {
                beforeApril = (account.getRemainingVacationDays().subtract(beforeApril)).negate();
            } else {
                beforeApril = BigDecimal.ZERO;
            }

            if ((account.getVacationDays().subtract(beforeApril.add(afterApril))).compareTo(BigDecimal.ZERO) >= 0) {
                return true;
            }
        }

        return false;
    }


    public boolean checkAntragTwoYears(Application application) {

        Person person = application.getPerson();
        DateMidnight start = application.getStartDate();
        DateMidnight end = application.getEndDate();

        HolidaysAccount accountCurrentYear = accountService.getHolidaysAccount(start.getYear(), person);
        HolidaysAccount accountNextYear = accountService.getHolidaysAccount(end.getYear(), person);

        if (accountCurrentYear == null) {
            accountCurrentYear = accountService.newHolidaysAccount(person,
                    accountService.getHolidayEntitlement(start.getYear(), person).getVacationDays(), BigDecimal.ZERO,
                    start.getYear());
        }

        if (accountNextYear == null) {
            accountNextYear = accountService.newHolidaysAccount(person,
                    accountService.getHolidayEntitlement(end.getYear(), person).getVacationDays(), BigDecimal.ZERO,
                    end.getYear());
        }

        BigDecimal beforeJan = calendarService.getVacationDays(application, application.getStartDate(),
                new DateMidnight(start.getYear(), DateTimeConstants.DECEMBER, LAST_DAY));
        BigDecimal afterJan = calendarService.getVacationDays(application,
                new DateMidnight(end.getYear(), DateTimeConstants.JANUARY, FIRST_DAY), application.getEndDate());

        if (((accountCurrentYear.getVacationDays().subtract(beforeJan)).compareTo(BigDecimal.ZERO) >= 0)
                && (((accountNextYear.getVacationDays().add(accountNextYear.getRemainingVacationDays())).subtract(
                            afterJan)).compareTo(BigDecimal.ZERO) >= 0)) {
            return true;
        }

        return false;
    }
}
