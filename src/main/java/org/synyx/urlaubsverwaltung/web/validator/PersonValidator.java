
package org.synyx.urlaubsverwaltung.web.validator;

import org.apache.log4j.Logger;

import org.joda.time.DateMidnight;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.mail.MailNotification;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.core.util.PropertiesUtil;
import org.synyx.urlaubsverwaltung.security.Role;
import org.synyx.urlaubsverwaltung.web.person.PersonForm;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class validate if a {@link PersonForm} is filled correctly by the user, else it saves error messages in errors
 * object.
 *
 * @author  Aljona Murygina
 */
@Component
public class PersonValidator implements Validator {

    private static final Logger LOG = Logger.getLogger(PersonValidator.class);

    private static final String ERROR_MANDATORY_FIELD = "error.mandatory.field";
    private static final String ERROR_ENTRY = "error.entry";
    private static final String ERROR_EMAIL = "error.email";
    private static final String ERROR_LENGTH = "error.length";
    private static final String ERROR_LOGIN_UNIQUE = "error.login.unique";

    private static final String LOGIN_NAME = "loginName";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ANNUAL_VACATION_DAYS = "annualVacationDays";
    private static final String REMAINING_VACATION_DAYS = "remainingVacationDays";
    private static final String EMAIL = "email";
    private static final String PERMISSIONS = "permissions";

    // a regex for email addresses that are valid, but may be "strange looking" (e.g. tomr$2@example.com)
    // original from: http://www.markussipila.info/pub/emailvalidator.php?action=validate
    // modified by adding following characters: äöüß
    private static final String EMAIL_PATTERN =
        "^[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+(\\.[a-zäöüß0-9,!#\\$%&'\\*\\+/=\\?\\^_`\\{\\|}~-]+)*@"
        + "[a-zäöüß0-9-]+(\\.[a-zäöüß0-9-]+)*\\.([a-z]{2,})$";

    private static final String MAX_DAYS = "annual.vacation.max";
    private static final int MAX_CHARS = 50;

    private static final String BUSINESS_PROPERTIES_FILE = "business.properties";

    private Properties businessProperties;

    private final PersonService personService;

    @Autowired
    public PersonValidator(PersonService personService) {

        this.personService = personService;

        try {
            this.businessProperties = PropertiesUtil.load(BUSINESS_PROPERTIES_FILE);
        } catch (IOException ex) {
            LOG.error("No properties file found.");
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> clazz) {

        return PersonForm.class.equals(clazz);
    }


    @Override
    public void validate(Object target, Errors errors) {

        PersonForm form = (PersonForm) target;

        validateName(form.getFirstName(), FIRST_NAME, errors);

        validateName(form.getLastName(), LAST_NAME, errors);

        validateEmail(form.getEmail(), errors);

        validatePeriod(form, errors);

        validateValidFrom(form, errors);

        validateAnnualVacation(form, errors);

        validateRemainingVacationDays(form, errors);

        validatePermissions(form, errors);

        validateNotifications(form, errors);
    }


    private boolean matchPattern(String nameOfPattern, String matchSequence) {

        Pattern pattern = Pattern.compile(nameOfPattern);
        Matcher matcher = pattern.matcher(matchSequence);

        return matcher.matches();
    }


    /**
     * This method ensures that the field firstName and the field lastName are not {@code null} or empty and not too
     * long.
     *
     * @param  name  (may be the field firstName or lastName)
     * @param  field
     * @param  errors
     */
    protected void validateName(String name, String field, Errors errors) {

        // is the name field null/empty?
        if (!StringUtils.hasText(name)) {
            errors.rejectValue(field, ERROR_MANDATORY_FIELD);
        } else {
            // is String length alright?
            if (!validateStringLength(name)) {
                errors.rejectValue(field, ERROR_LENGTH);
            }
        }
    }


    public void validateLogin(String login, Errors errors) {

        validateName(login, LOGIN_NAME, errors);

        if (!errors.hasFieldErrors(LOGIN_NAME)) {
            // validate unique login name
            if (personService.getPersonByLogin(login) != null) {
                errors.rejectValue(LOGIN_NAME, ERROR_LOGIN_UNIQUE);
            }
        }
    }


    /**
     * This method checks if the field email is filled and if it is filled, it validates the entry with a regex.
     *
     * @param  email
     * @param  errors
     */
    protected void validateEmail(String email, Errors errors) {

        // is email field null or empty
        if (!StringUtils.hasText(email)) {
            errors.rejectValue(EMAIL, ERROR_MANDATORY_FIELD);
        } else {
            // String length alright?
            if (!validateStringLength(email)) {
                errors.rejectValue(EMAIL, ERROR_LENGTH);
            }

            // validation with regex
            String normalizedEmail = email.trim().toLowerCase();

            if (!matchPattern(EMAIL_PATTERN, normalizedEmail)) {
                errors.rejectValue(EMAIL, ERROR_EMAIL);
            }
        }
    }


    protected void validatePeriod(PersonForm form, Errors errors) {

        DateMidnight holidaysAccountValidFrom = form.getHolidaysAccountValidFrom();
        DateMidnight holidaysAccountValidTo = form.getHolidaysAccountValidTo();

        validateDateNotNull(holidaysAccountValidFrom, "holidaysAccountValidFrom", errors);
        validateDateNotNull(holidaysAccountValidTo, "holidaysAccountValidTo", errors);

        if (holidaysAccountValidFrom != null && holidaysAccountValidTo != null) {
            boolean periodIsNotWithinOneYear = holidaysAccountValidFrom.getYear() != form.getHolidaysAccountYear()
                || holidaysAccountValidTo.getYear() != form.getHolidaysAccountYear();
            boolean periodIsOnlyOneDay = holidaysAccountValidFrom.equals(holidaysAccountValidTo);
            boolean beginOfPeriodIsAfterEndOfPeriod = holidaysAccountValidFrom.isAfter(holidaysAccountValidTo);

            if (periodIsNotWithinOneYear || periodIsOnlyOneDay || beginOfPeriodIsAfterEndOfPeriod) {
                errors.reject("error.period");
            }
        }
    }


    private void validateDateNotNull(DateMidnight date, String field, Errors errors) {

        if (date == null) {
            // may be that date field is null because of cast exception, than there is already a field error
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, ERROR_MANDATORY_FIELD);
            }
        }
    }


    private void validateValidFrom(PersonForm form, Errors errors) {

        validateDateNotNull(form.getValidFrom(), "validFrom", errors);
    }


    protected void validateAnnualVacation(PersonForm form, Errors errors) {

        // only achieved if invalid property values are precluded by method validateProperties
        String propValue = businessProperties.getProperty(MAX_DAYS);
        double max = Double.parseDouble(propValue);

        BigDecimal annualVacationDays = form.getAnnualVacationDays();

        validateNumberNotNull(annualVacationDays, ANNUAL_VACATION_DAYS, errors);

        if (annualVacationDays != null) {
            validateNumberOfDays(annualVacationDays, ANNUAL_VACATION_DAYS, max, errors);
        }
    }


    private void validateNumberNotNull(BigDecimal number, String field, Errors errors) {

        if (number == null) {
            // may be that number field is null because of cast exception, than there is already a field error
            if (errors.getFieldErrors(field).isEmpty()) {
                errors.rejectValue(field, ERROR_MANDATORY_FIELD);
            }
        }
    }


    protected void validateRemainingVacationDays(PersonForm form, Errors errors) {

        // only achieved if invalid property values are precluded by method validateProperties
        String propValue = businessProperties.getProperty(MAX_DAYS);
        double max = Double.parseDouble(propValue);

        BigDecimal remainingVacationDays = form.getRemainingVacationDays();

        validateNumberNotNull(remainingVacationDays, REMAINING_VACATION_DAYS, errors);

        if (remainingVacationDays != null) {
            // field entitlement's remaining vacation days
            validateNumberOfDays(remainingVacationDays, REMAINING_VACATION_DAYS, max, errors);
        }
    }


    /**
     * This method validates if the holiday entitlement's fields remaining vacation days and vacation days are filled
     * and if they are filled, it checks if the number of days is realistic.
     *
     * @param  days
     * @param  field
     * @param  maximumDays
     * @param  errors
     */
    private void validateNumberOfDays(BigDecimal days, String field, double maximumDays, Errors errors) {

        // is number of days < 0 ?
        if (days.compareTo(BigDecimal.ZERO) == -1) {
            errors.rejectValue(field, ERROR_ENTRY);
        }

        // is number of days unrealistic?
        if (days.compareTo(BigDecimal.valueOf(maximumDays)) == 1) {
            errors.rejectValue(field, ERROR_ENTRY);
        }
    }


    /**
     * Checks if a String has a valid length.
     *
     * @param  string
     *
     * @return
     */
    protected boolean validateStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    protected void validatePermissions(PersonForm personForm, Errors errors) {

        List<Role> roles = personForm.getPermissions();

        if (roles == null || roles.isEmpty()) {
            errors.rejectValue(PERMISSIONS, "role.error.least");
        } else {
            // if role inactive set, then only this role may be selected
            // else this is an error

            boolean roleInactiveSet = false;

            for (Role role : roles) {
                if (role == Role.INACTIVE) {
                    roleInactiveSet = true;
                }
            }

            if (roleInactiveSet) {
                // validate that there is only role inactive set
                // this means size of role collection must have size 1
                if (roles.size() != 1) {
                    errors.rejectValue(PERMISSIONS, "role.error.inactive");
                }
            }
        }
    }


    protected void validateNotifications(PersonForm personForm, Errors errors) {

        List<Role> roles = personForm.getPermissions();
        List<MailNotification> notifications = personForm.getNotifications();

        if (roles != null) {
            boolean bossNotificationsSelectedButNotBossRole = notifications.contains(MailNotification.NOTIFICATION_BOSS)
                && !roles.contains(Role.BOSS);
            boolean officeNotificationsSelectedButNotOfficeRole = notifications.contains(
                    MailNotification.NOTIFICATION_OFFICE) && !roles.contains(Role.OFFICE);

            if (bossNotificationsSelectedButNotBossRole || officeNotificationsSelectedButNotOfficeRole) {
                errors.rejectValue("notifications", "notification.error");
            }
        }
    }
}
