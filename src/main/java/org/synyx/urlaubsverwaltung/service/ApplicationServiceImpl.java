
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.dao.ApplicationDAO;
import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import java.util.List;

/**
 * Implementation of interface {@link ApplicationService}.
 * 
 * @author Aljona Murygina - murygina@synyx.de
 */
public class ApplicationServiceImpl implements ApplicationService {

    // sign logger: logs possible occurent errors relating to private and public keys of users
    private static final Logger LOG_SIGN = Logger.getLogger("sign");
    
    private ApplicationDAO applicationDAO;
    
    private CryptoService cryptoService;
    private MailService mailService;

    @Autowired
    public ApplicationServiceImpl(ApplicationDAO applicationDAO, CryptoService cryptoService, MailService mailService) {
        this.applicationDAO = applicationDAO;
        this.cryptoService = cryptoService;
        this.mailService = mailService;
    }
    
    @Override
    public int getIdOfLatestApplication(Person person, ApplicationStatus status) {
        return applicationDAO.getIdOfLatestApplication(person, status);
    }

    @Override
    public Application getApplicationById(Integer id) {
        return applicationDAO.findOne(id);
    }

    @Override
    public void save(Application application) {
        applicationDAO.save(application);
    }

    @Override
    public void allow(Application application, Person boss) {
        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());

        // set state on allowed
        application.setStatus(ApplicationStatus.ALLOWED);

        // sign application and save it
        signApplicationByBoss(application, boss);
        
        save(application);
    }

    @Override
    public void reject(Application application, Person boss) {
        application.setStatus(ApplicationStatus.REJECTED);

        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.REJECTED);

        application.setBoss(boss);
        application.setEditedDate(DateMidnight.now());
        
        save(application);
    }

    @Override
    public void cancel(Application application) {
        // are there any supplemental applications?
        // change their status too
        setStatusOfSupplementalApplications(application, ApplicationStatus.CANCELLED);

        application.setStatus(ApplicationStatus.CANCELLED);
        application.setCancelDate(DateMidnight.now());
        
        save(application);
    }

    @Override
    public void signApplicationByUser(Application application, Person user) {
         byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
            applicationDAO.save(application);
        }
    }

    @Override
    public void signApplicationByBoss(Application application, Person boss) {
        byte[] data = signApplication(application, boss);

        if (data != null) {
            application.setSignatureBoss(data);
            applicationDAO.save(application);
        }
    }
    
        /**
     * generates signature (byte[]) by private key of person
     *
     * @param  application
     * @param  person
     *
     * @return  data (=signature) if using cryptoService was successful or null if there was any mistake
     */
    private byte[] signApplication(Application application, Person person) {

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
            logSignException(application.getId(), ex);
        } catch (SignatureException ex) {
            logSignException(application.getId(), ex);
        } catch (NoSuchAlgorithmException ex) {
            logSignException(application.getId(), ex);
        } catch (InvalidKeySpecException ex) {
            logSignException(application.getId(), ex);
        }

        return null;
    }


    /**
     * This method logs exception's details and sends an email to inform the tool manager that an error occured while
     * signing the application.
     *
     * @param  applicationId
     * @param  ex
     */
    private void logSignException(Integer applicationId, Exception ex) {

        LOG_SIGN.error("An error occured during signing application with id " + applicationId, ex);
        mailService.sendSignErrorNotification(applicationId, ex.getMessage());
    }
    
        /**
     * If an application that spans December and January is cancelled or rejected, the supplemental applications of this
     * application have to get the new status too.
     *
     * @param  application
     * @param  state
     */
    private void setStatusOfSupplementalApplications(Application application, ApplicationStatus state) {

        // are there any supplemental applications?
        if (application.getStartDate().getYear() != application.getEndDate().getYear()) {
            // if an application spans December and January, it has to own two supplementary applications

            List<Application> sApps = applicationDAO.getSupplementalApplicationsForApplication(application.getId());

            // edit status of supplemental applications too
            for (Application sa : sApps) {
                sa.setStatus(state);
                save(sa);
            }
        }
    }
    
}
