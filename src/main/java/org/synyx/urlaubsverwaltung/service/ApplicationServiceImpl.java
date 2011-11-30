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

    @Override
    public Application getApplicationById(Integer id) {

        return applicationDAO.findOne(id);
    }


    /**
     * @see  ApplicationService#getAllApplicationsForPerson(org.synyx.urlaubsverwaltung.domain.Person)
     */
    @Override
    public List<Application> getAllApplicationsForPerson(Person person) {

        return applicationDAO.getAllApplicationsForPerson(person);
    }


    /**
     * @see  ApplicationService#getAllApplicationsByState(org.synyx.urlaubsverwaltung.domain.ApplicationStatus)
     */
    @Override
    public List<Application> getAllApplicationsByState(ApplicationStatus state) {

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

        mailService.sendAllowedNotification(application);
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
            HolidaysAccount account = accountService.getAccountOrCreateOne(start.getYear(), person);

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

        mailService.sendRejectedNotification(application);
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
            mailService.sendCancelledNotification(application, true);
        } else if (application.getStatus() == ApplicationStatus.ALLOWED) {
            application.setStatus(ApplicationStatus.CANCELLED);
            applicationDAO.save(application);

            // if application has been allowed, office gets email
            mailService.sendCancelledNotification(application, false);
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
            HolidaysAccount account = accountService.getAccountOrCreateOne(end.getYear(), person);

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

        byte[] data = signApplication(application, boss);

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

        byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }


    // exceptions have to be catched!
    /**
     * generates signature (byte[]) by private key of person
     *
     * @param  application
     * @param  person
     *
     * @return  data (=signature) if using cryptoService was successful or null if there was any mistake
     */
    public byte[] signApplication(Application application, Person person) {

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
     * @see  ApplicationService#checkApplication(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public boolean checkApplication(Application application) {

        if (application.getStartDate().getYear() == application.getEndDate().getYear()) {
            return calculationService.checkApplicationOneYear(application);
        } else {
            return calculationService.checkApplicationTwoYears(application);
        }
    }
}
