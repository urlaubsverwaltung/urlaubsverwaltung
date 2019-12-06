package org.synyx.urlaubsverwaltung.mail;

public class MailBuilderException extends RuntimeException {

    MailBuilderException(String message, Exception e) {
        super(message, e);
    }
}
