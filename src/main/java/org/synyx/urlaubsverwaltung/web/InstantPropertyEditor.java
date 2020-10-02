package org.synyx.urlaubsverwaltung.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.util.DateFormat;

import java.beans.PropertyEditorSupport;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantPropertyEditor extends PropertyEditorSupport {

    private static final Logger log = LoggerFactory.getLogger(InstantPropertyEditor.class);

    private final SettingsService settingsService;
    private final Clock systemClock;
    private final DateTimeFormatter formatter;

    public InstantPropertyEditor(Clock systemClock, SettingsService settingsService) {

        this.systemClock = systemClock;
        this.settingsService = settingsService;
        this.formatter = DateTimeFormatter.ofPattern(DateFormat.PATTERN);
    }

    // Instant to String
    @Override
    public String getAsText() {

        if (this.getValue() == null) {
            return "";
        }

        Instant instant = (Instant) this.getValue();
        ZoneId zoneId = ZoneId.of(settingsService.getSettings().getTimeSettings().getTimeZoneId());
        log.warn("Write Instant: " + instant + " with zoneId: " + zoneId);
        return formatter.format(instant.atZone(zoneId));
    }


    // String to Instant
    @Override
    public void setAsText(String text) {

        if (!StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            try {
                this.setValue(Instant.from(formatter.parse(text)).atZone(systemClock.getZone()));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(exception.getMessage());
            }
        }
    }
}
