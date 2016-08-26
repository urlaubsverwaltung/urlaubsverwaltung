package org.synyx.urlaubsverwaltung.web.settings;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.synyx.urlaubsverwaltung.core.settings.MailSettings;
import org.synyx.urlaubsverwaltung.web.MailAddressValidationUtil;


/**
 * Validates {@link org.synyx.urlaubsverwaltung.core.settings.MailSettings}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Component
class MailSettingsValidator implements Validator {

    private static final String ERROR_MANDATORY_FIELD = "error.entry.mandatory";
    private static final String ERROR_INVALID_ENTRY = "error.entry.invalid";
    private static final String ERROR_INVALID_EMAIL = "error.entry.mail";
    private static final String ERROR_LENGTH = "error.entry.tooManyChars";

    private static final int MAX_CHARS = 255;

    @Override
    public boolean supports(Class<?> clazz) {

        return clazz.equals(MailSettings.class);
    }


    @Override
    public void validate(Object target, Errors errors) {

        Assert.isTrue(supports(target.getClass()), "The given object must be an instance of MailSettings");

        MailSettings mailSettings = (MailSettings) target;

        if (mailSettings.isActive()) {
            validateMailHost(mailSettings, errors);

            validateMailPort(mailSettings, errors);

            validateMailUsername(mailSettings, errors);

            validateMailPassword(mailSettings, errors);

            validateMailFrom(mailSettings, errors);

            validateMailAdministrator(mailSettings, errors);

            validateMailBaseLinkURL(mailSettings, errors);
        }
    }


    private void validateMailHost(MailSettings mailSettings, Errors errors) {

        String hostAttribute = "host";
        String host = mailSettings.getHost();

        validateMandatoryTextField(host, hostAttribute, errors);
    }


    private void validateMandatoryTextField(String input, String attributeName, Errors errors) {

        if (!StringUtils.hasText(input)) {
            errors.rejectValue(attributeName, ERROR_MANDATORY_FIELD);
        } else {
            if (!validStringLength(input)) {
                errors.rejectValue(attributeName, ERROR_LENGTH);
            }
        }
    }


    private boolean validStringLength(String string) {

        return string.length() <= MAX_CHARS;
    }


    private void validateMailPort(MailSettings mailSettings, Errors errors) {

        String portAttribute = "port";
        Integer port = mailSettings.getPort();

        if (port == null) {
            errors.rejectValue(portAttribute, ERROR_MANDATORY_FIELD);
        } else {
            if (port <= 0) {
                errors.rejectValue(portAttribute, ERROR_INVALID_ENTRY);
            }
        }
    }


    private void validateMailUsername(MailSettings mailSettings, Errors errors) {

        String username = mailSettings.getUsername();

        if (username != null && !validStringLength(username)) {
            errors.rejectValue("username", ERROR_LENGTH);
        }
    }


    private void validateMailPassword(MailSettings mailSettings, Errors errors) {

        String password = mailSettings.getPassword();

        if (password != null && !validStringLength(password)) {
            errors.rejectValue("password", ERROR_LENGTH);
        }
    }


    private void validateMailFrom(MailSettings mailSettings, Errors errors) {

        String fromAttribute = "from";
        String from = mailSettings.getFrom();

        validateMandatoryMailAddress(from, fromAttribute, errors);
    }


    private void validateMandatoryMailAddress(String mailAddress, String attributeName, Errors errors) {

        if (!StringUtils.hasText(mailAddress)) {
            errors.rejectValue(attributeName, ERROR_MANDATORY_FIELD);
        } else {
            if (!validStringLength(mailAddress)) {
                errors.rejectValue(attributeName, ERROR_LENGTH);
            }

            if (!MailAddressValidationUtil.hasValidFormat(mailAddress)) {
                errors.rejectValue(attributeName, ERROR_INVALID_EMAIL);
            }
        }
    }


    private void validateMailAdministrator(MailSettings mailSettings, Errors errors) {

        String administratorAttribute = "administrator";
        String administrator = mailSettings.getAdministrator();

        validateMandatoryMailAddress(administrator, administratorAttribute, errors);
    }


    private void validateMailBaseLinkURL(MailSettings mailSettings, Errors errors) {

        String baseLinkURLAttribute = "baseLinkURL";
        String baseLinkURL = mailSettings.getBaseLinkURL();

        validateMandatoryTextField(baseLinkURL, baseLinkURLAttribute, errors);
    }
}
