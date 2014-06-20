/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.web.validator;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.validation.Errors;

import org.synyx.urlaubsverwaltung.core.mail.MailService;
import org.synyx.urlaubsverwaltung.web.application.AppForm;

import java.util.Properties;


/**
 * @author  Aljona Murygina
 *
 *          <p>This class checks if the tested property keys have a valid value. If not, the tool manager is notified
 *          and errors objects are filled.</p>
 */
@Component
public class PropertiesValidator {

    // property keys (custom properties)
    private static final String MAX_DAYS = "annual.vacation.max";
    private static final String MAX_MONTHS = "maximum.months";

    // error messages (messages properties)
    private static final String ERROR_STH_WRONG = "error.sth.went.wrong";
    private static final String ERROR_TOO_LONG = "error.too.long";

    private MailService mailService;

    @Autowired
    public PropertiesValidator(MailService mailService) {

        this.mailService = mailService;
    }

    /**
     * This method checks if the value of property with key 'annual.vacation.max' is valid. If it's not, the tool
     * manager is notified and editing the person is not possible.
     *
     * @param  properties
     * @param  errors
     */
    public void validateAnnualVacationProperty(Properties properties, Errors errors) {

        String propValue = properties.getProperty(MAX_DAYS);

        try {
            double max = Double.parseDouble(propValue);

            if (max < 1 || max > 366) {
                errors.reject(ERROR_STH_WRONG);
                mailService.sendPropertiesErrorNotification(MAX_DAYS);
            }
        } catch (NumberFormatException ex) {
            errors.reject(ERROR_STH_WRONG);
            mailService.sendPropertiesErrorNotification(MAX_DAYS);
        }
    }


    public void validateMaximumVacationProperty(Properties properties, AppForm app, Errors errors) {

        // applying for leave maximum permissible x months in advance
        String propValue = properties.getProperty(MAX_MONTHS);

        try {
            int x = Integer.parseInt(propValue);

            if (x > 0 && x < 37) {
                DateMidnight future = DateMidnight.now().plusMonths(x);

                if (app.getEndDate().isAfter(future)) {
                    errors.reject(ERROR_TOO_LONG);
                }
            } else {
                errors.reject(ERROR_STH_WRONG);
                mailService.sendPropertiesErrorNotification(MAX_MONTHS);
            }
        } catch (NumberFormatException ex) {
            errors.reject(ERROR_STH_WRONG);
            mailService.sendPropertiesErrorNotification(MAX_MONTHS);
        }
    }
}
