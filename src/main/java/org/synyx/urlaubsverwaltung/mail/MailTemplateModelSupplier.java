package org.synyx.urlaubsverwaltung.mail;

import java.util.Locale;
import java.util.Map;

@FunctionalInterface
public interface MailTemplateModelSupplier {

    Map<String, Object> getMailTemplateModel(Locale locale);
}
