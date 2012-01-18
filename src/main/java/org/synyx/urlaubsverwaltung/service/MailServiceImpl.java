package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

import org.apache.velocity.app.VelocityEngine;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import org.springframework.ui.velocity.VelocityEngineUtils;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 *
 *          <p>nice tutorial: http://static.springsource.org/spring/docs/2.0.5/reference/mail.html</p>
 */
public class MailServiceImpl implements MailService {

    private static final Logger mailLogger = Logger.getLogger("mailLogger");

    private static final String FROM = "email.manager";

    private static final String PATH = "/email/";
    private static final String APPLICATION = "application";
    private static final String PERSON = "person";
    private static final String PERSONS = "persons";

    // File names
    private static final String TYPE = ".vm";
    private static final String FILE_ALLOWED_OFFICE = "allowed_office" + TYPE;
    private static final String FILE_ALLOWED_USER = "allowed_user" + TYPE;
    private static final String FILE_CANCELLED = "cancelled" + TYPE;
    private static final String FILE_CONFIRM = "confirm" + TYPE;
    private static final String FILE_EXPIRE = "expire" + TYPE;
    private static final String FILE_NEW = "newapplications" + TYPE;
    private static final String FILE_REJECTED = "rejected" + TYPE;
    private static final String FILE_WEEKLY = "weekly" + TYPE;

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;
    }

    /**
     * this method prepares an email:
     *
     * @param  object  e.g. person or application that should be put in a model object
     * @param  modelName
     * @param  fileName  name of email's template file
     *
     * @return  String text that must be put in the email as text (sending is done by method sendEmail)
     */
    private String prepareMessage(Object object, String modelName, String fileName) {

        Map model = new HashMap();
        model.put(modelName, object);

        // mergeTemplateIntoString(VelocityEngine velocityEngine, String templateLocation, Map model)
        String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, PATH + fileName, model);

        return text;
    }


    /**
     * this method gets the recipient email address, the email's subject and text with this parameters an email is build
     * and sent
     *
     * @param  recipient
     * @param  subject
     * @param  text
     */
    private void sendEmail(final String recipient, final String subject, final String text) {

        MimeMessagePreparator prep = new MimeMessagePreparator() {

            public void prepare(MimeMessage mimeMessage) throws MessagingException {

                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                mimeMessage.setFrom(new InternetAddress(FROM));
                mimeMessage.setSubject(subject);
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            mailLogger.error(ex.getMessage(), ex);
        }
    }


    /**
     * @see  MailService#sendExpireNotification(java.util.List)
     */
    @Override
    public void sendExpireNotification(List<Person> persons) {

        for (Person person : persons) {
            String text = prepareMessage(person, PERSON, FILE_EXPIRE);

            sendEmail(person.getEmail(), "subject.expire", text);
        }
    }


    /**
     * @see  MailService#sendNewApplicationsNotification(java.util.List)
     */
    @Override
    public void sendNewApplicationsNotification(List<Application> applications) {

        for (Application application : applications) {
            String text = prepareMessage(application, APPLICATION, FILE_NEW);

            sendEmail("email.chefs", "subject.new", text);
        }
    }


    /**
     * @see  MailService#sendAllowedNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendAllowedNotification(Application application) {

        // if application has been allowed, two emails must be sent
        // the applicant gets an email and the office gets an email

        // email to office
        String textOffice = prepareMessage(application, APPLICATION, FILE_ALLOWED_OFFICE);
        sendEmail("email.office", "subject.allowed.office", textOffice);

        // email to applicant
        String textUser = prepareMessage(application, APPLICATION, FILE_ALLOWED_USER);
        sendEmail(application.getPerson().getEmail(), "subject.allowed.user", textUser);
    }


    /**
     * @see  MailService#sendRejectedNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendRejectedNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_REJECTED);
        sendEmail(application.getPerson().getEmail(), "subject.rejected", text);
    }


    /**
     * @see  MailService#sendConfirmation(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendConfirmation(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_CONFIRM);
        sendEmail(application.getPerson().getEmail(), "subject.confirm", text);
    }


    /**
     * @see  MailService#sendWeeklyVacationForecast(java.util.List)
     */
    @Override
    public void sendWeeklyVacationForecast(List<Person> persons) {

        List<String> names = new ArrayList<String>();

        for (Person person : persons) {
            names.add(person.getFirstName() + " " + person.getLastName() + "\n");
        }

        String text = prepareMessage(names, PERSONS, FILE_WEEKLY);
        sendEmail("email.all", "subject.weekly", text);
    }


    /**
     * @see  MailService#sendCancelledNotification(org.synyx.urlaubsverwaltung.domain.Application, boolean)
     */
    @Override
    public void sendCancelledNotification(Application application, boolean isBoss) {

        String text = prepareMessage(application, APPLICATION, FILE_CANCELLED);

        // isBoss  describes if chefs (param is true) or office (param is false) get the email
        // (dependent on application's state: waiting-chefs, allowed-office)

        if (isBoss) {
            sendEmail("email.chefs", "subject.cancelled", text);
        } else {
            sendEmail("email.office", "subject.cancelled", text);
        }
    }

    /**
    * NOT YET IMPLEMENTED
    * Commented out on Tu, 2011/11/29 - Aljona Murygina
    * Think about if method really is necessary or not
    *
    * @see  MailService#sendBalance(java.lang.Object)
    */

// @Override
// public void sendBalance(Object balanceObject) {
//
// throw new UnsupportedOperationException("Not supported yet.");
// }

}
