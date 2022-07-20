package org.synyx.urlaubsverwaltung.web.thymeleaf;

import org.springframework.context.MessageSource;
import org.springframework.format.Printer;
import org.synyx.urlaubsverwaltung.util.DurationFormatter;

import java.time.Duration;
import java.util.Locale;

class DurationPrinter implements Printer<Duration> {

    private final MessageSource messageSource;

    DurationPrinter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String print(Duration object, Locale locale) {
        return DurationFormatter.toDurationString(object, messageSource, locale);
    }
}
