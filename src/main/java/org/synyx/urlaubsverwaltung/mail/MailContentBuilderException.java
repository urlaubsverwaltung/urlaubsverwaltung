package org.synyx.urlaubsverwaltung.mail;

public class MailContentBuilderException extends RuntimeException {

    MailContentBuilderException(String message, Exception e) {
        super(message, e);
    }
}
