/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.service;

import org.synyx.urlaubsverwaltung.domain.Application;
import org.synyx.urlaubsverwaltung.domain.Person;

import java.util.List;


/**
 * the mailservice provides sending of notification-mails to the users, bosses and office
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */

public interface MailService {

    /**
     * this method sends an email to all persons of the given list that their remaining vacation days are going to
     * expire soon
     *
     * @param  persons  the list of persons which should receive the email
     */
    void sendExpireNotification(List<Person> persons);


    /**
     * this method sends an email to the applicant that the application has been made successfully
     *
     * @param  application
     */
    void sendConfirmation(Application application);


    /**
     * this method sends an email to the person of the given application that the office has applied for leave on behalf
     * of himself
     *
     * @param  application
     */
    void sendAppliedForLeaveByOfficeNotification(Application application);


    /**
     * this method sends an email to the bosses notifying that there is a new application for leave which has to be
     * allowed or rejected by a boss
     *
     * @param  application
     */
    void sendNewApplicationNotification(Application application);


    /**
     * this method sends an email to the applicant and to the office that the application has been allowed
     *
     * @param  application  the application which got allowed
     */
    void sendAllowedNotification(Application application);


    /**
     * this method sends an email to the applicant that the application has been rejected
     *
     * @param  application  the application which got rejected
     */
    void sendRejectedNotification(Application application);
    
    /**
     * If a boss is not sure about the decision of an application (reject or allow), he can ask another boss to decide about this application via a generated email. 
     * @param a
     * @param p 
     */
    void sendReferApplicationNotification(Application a, Person reciever, String sender);


    /**
     * this method sends a newsletter mentioning all person that are on holiday
     *
     * @param  persons  persons on vacation this week
     */
    void sendWeeklyVacationForecast(List<Person> persons);


    /**
     * This method sends an email if an application got cancelled. If the application had the status allowed and was
     * cancelled by the applicant, the office gets an email. If the application was cancelled by the office (regardless
     * of which status), the applicant gets an email.
     *
     * @param  application  the application which got canceled
     * @param  cancelledByOffice  describes if chefs (param is true) or office (param is false) get the email (dependent
     *                            on application's state: waiting-chefs, allowed-office
     */
    void sendCancelledNotification(Application application, boolean cancelledByOffice);


    /**
     * this method sends an email to the tool's manager to inform if an error occured while generating private and
     * public key for a new user with the given login name
     *
     * @param  loginName
     */
    void sendKeyGeneratingErrorNotification(String loginName);


    /**
     * this method sends an email to the tool's manager to inform if an error occured while signing an application with
     * the given id
     *
     * @param  applicationId
     * @param  exception
     */
    void sendSignErrorNotification(Integer applicationId, String exception);


    /**
     * this method sends an email to the tool's manager to inform if an error occured because of invalid defined
     * property value (e.g. "maximum.months")
     *
     * @param  name  of the concerned property key
     */
    void sendPropertiesErrorNotification(String propertyName);

    /**
     * Commented out on Tu, 2011/11/29 - Aljona Murygina
     * Think about if method really is necessary or not
     *
     * This method sends a yearly balance to the certain user
     *
     * @param  balanceObject
     */
    // data-bean that owns bilance data
    // void sendBalance(Object balanceObject);
}
