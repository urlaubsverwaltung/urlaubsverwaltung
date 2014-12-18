package org.synyx.urlaubsverwaltung.core.application.service;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.security.CryptoUtil;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;


/**
 * Signs application when its state changes.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class SignService {

    private static final Logger LOG = Logger.getLogger(SignService.class);

    private final MailService mailService;

    @Autowired
    public SignService(MailService mailService) {

        this.mailService = mailService;
    }

    /**
     * Generates applier signature of application for leave.
     *
     * @param  application
     * @param  user
     */
    public void signApplicationByUser(Application application, Person user) {

        byte[] data = signApplication(application, user);

        if (data != null) {
            application.setSignaturePerson(data);
        }
    }


    /**
     * Generates allower/rejecter signature of application for leave.
     *
     * @param  application
     * @param  boss
     */
    public void signApplicationByBoss(Application application, Person boss) {

        byte[] data = signApplication(application, boss);

        if (data != null) {
            application.setSignatureBoss(data);
        }
    }


    /**
     * Generates signature (byte[]) by private key of {@link Person}.
     *
     * @param  application {@link Application}
     * @param  person {@link Person}
     *
     * @return  data (=signature) if using cryptoService was successful or null if there was any mistake
     */
    private byte[] signApplication(Application application, Person person) {

        try {
            PrivateKey privKey = CryptoUtil.getPrivateKeyByBytes(person.getPrivateKey());

            StringBuilder build = new StringBuilder();

            build.append(application.getPerson().getLastName());
            build.append(application.getApplicationDate().toString());
            build.append(application.getVacationType().toString());

            byte[] data = build.toString().getBytes();

            data = CryptoUtil.sign(privKey, data);

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
     * This method logs exception's details and sends an email to inform the tool manager that an error occurred while
     * signing the application.
     *
     * @param  applicationId  Integer
     * @param  ex  Exception
     */
    private void logSignException(Integer applicationId, Exception ex) {

        LOG.error("An error occurred during signing application with id " + applicationId, ex);
        mailService.sendSignErrorNotification(applicationId, ex.getMessage());
    }
}
