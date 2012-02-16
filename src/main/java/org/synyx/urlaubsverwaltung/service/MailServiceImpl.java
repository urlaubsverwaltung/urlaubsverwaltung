package org.synyx.urlaubsverwaltung.service;

import org.apache.log4j.Logger;

import org.apache.velocity.app.VelocityEngine;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import org.springframework.ui.velocity.VelocityEngineUtils;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 *
 *          <p>nice tutorial: http://static.springsource.org/spring/docs/2.0.5/reference/mail.html</p>
 *
 *          <p>At the moment properties' values are set hard coded in this class (bad solution...): it would be better
 *          to read in the properties file (outcommented methods), but this was not successful yet...</p>
 */
public class MailServiceImpl implements MailService {

    private static final Logger LOG = Logger.getLogger(MailServiceImpl.class);

    private static final String DATE_FORMAT = "dd.MM.yyyy";

    private static final String PATH = "/email/";

    private static final String PROPERTIES_FILE = "messages_de.properties"; // genereal properties
    private static final String CUSTOM_PROPERTIES_FILE = "custom.properties"; // custom configuration like email
                                                                              // addresses, etc.

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
    private Properties properties;
    private Properties customProperties;

    @Autowired
    public MailServiceImpl(JavaMailSender mailSender, VelocityEngine velocityEngine) {

        this.mailSender = mailSender;
        this.velocityEngine = velocityEngine;

        try {
            this.properties = load(PROPERTIES_FILE);
            this.customProperties = load(CUSTOM_PROPERTIES_FILE);
        } catch (Exception ex) {
            LOG.error(DateMidnight.now().toString(DATE_FORMAT) + "No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    /**
     * Load a properties file from the classpath. Thanks to: http://www.rgagnon.com/javadetails/java-0434.html
     *
     * @param  propsName
     *
     * @return  Properties
     *
     * @throws  Exception
     */
    public Properties load(String propsName) throws Exception {

        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(propsName);
        props.load(url.openStream());

        return props;
    }


    /**
     * This method gets the properties' value of the given key.
     *
     * @param  key
     *
     * @return  String: value to the given key
     */
    private String getProperty(String key) {

        return properties.getProperty(key);
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

        if (object.getClass().equals(Application.class)) {
            Application a = (Application) object;
            String vacType = a.getVacationType().getVacationTypeName();
            String length = a.getHowLong().getDayLength();
            model.put("vacationType", getProperty(vacType));
            model.put("dayLength", getProperty(length));
        }

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

                mimeMessage.setSubject(customProperties.getProperty(subject));
                mimeMessage.setText(text);
            }
        };

        try {
            this.mailSender.send(prep);
        } catch (MailException ex) {
            LOG.error(DateMidnight.now().toString(DATE_FORMAT) + ": Sending the email with following subject '"
                + subject
                + "' to " + recipient + " failed.");
            LOG.error(ex.getMessage(), ex);
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
     * @see  MailService#sendNewApplicationNotification(org.synyx.urlaubsverwaltung.domain.Application)
     */
    @Override
    public void sendNewApplicationNotification(Application application) {

        String text = prepareMessage(application, APPLICATION, FILE_NEW);

        sendEmail(customProperties.getProperty("email.boss"), "subject.new", text);
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
        sendEmail(customProperties.getProperty("email.office"), "subject.allowed.office", textOffice);

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
        sendEmail(customProperties.getProperty("email.all"), "subject.weekly", text);
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
            sendEmail(customProperties.getProperty("email.boss"), "subject.cancelled", text);
        } else {
            sendEmail(customProperties.getProperty("email.office"), "subject.cancelled", text);
        }
    }


    @Override
    public void sendKeyGeneratingErrorNotification(String loginName) {

        String text = "An error occured during key generation for person with login " + loginName + " failed.";
        sendEmail(customProperties.getProperty("email.manager"), "subject.key.error", text);
    }


    @Override
    public void sendSignErrorNotification(Integer applicationId, String exception) {

        String text = "An error occured while signing the application with id " + applicationId + "\n" + exception;

        sendEmail(customProperties.getProperty("email.manager"), "subject.sign.error", text);
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
