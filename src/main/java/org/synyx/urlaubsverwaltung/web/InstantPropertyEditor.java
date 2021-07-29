package org.synyx.urlaubsverwaltung.web;

import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.absence.settings.TimeSettingsService;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import java.beans.PropertyEditorSupport;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantPropertyEditor extends PropertyEditorSupport {

    private final TimeSettingsService timeSettingsService;
    private final Clock clock;
    private final DateTimeFormatter formatter;

    public InstantPropertyEditor(Clock clock, TimeSettingsService timeSettingsService) {

        this.clock = clock;
        this.timeSettingsService = timeSettingsService;
        this.formatter = DateTimeFormatter.ofPattern(DateFormat.DD_MM_YYYY);
    }

    // Instant to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        Instant instant = (Instant) this.getValue();
        ZoneId zoneId = ZoneId.of(timeSettingsService.getSettings().getTimeZoneId());
        return formatter.format(instant.atZone(zoneId));
    }


    // String to Instant
    @Override
    public void setAsText(String text) {

        if (!StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            try {
                this.setValue(Instant.from(formatter.parse(text)).atZone(clock.getZone()));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(exception.getMessage());
            }
        }
    }
}
