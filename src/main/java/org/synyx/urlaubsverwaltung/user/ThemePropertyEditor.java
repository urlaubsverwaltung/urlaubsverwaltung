package org.synyx.urlaubsverwaltung.user;

import org.slf4j.Logger;

import java.beans.PropertyEditorSupport;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.synyx.urlaubsverwaltung.user.Theme.SYSTEM;

/**
 * Converts a {@link String} to {@link Theme} and vice versa.
 *
 * <p>A theme is a display preference. A name that cannot be mapped is no reason to reject the request, therefore
 * anything unknown becomes {@link Theme#SYSTEM}.
 */
public class ThemePropertyEditor extends PropertyEditorSupport {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    // Theme to String
    @Override
    public String getAsText() {
        return this.getValue() == null ? "" : ((Theme) this.getValue()).name();
    }

    // String to Theme
    @Override
    public void setAsText(String text) {
        this.setValue(toTheme(text));
    }

    private static Theme toTheme(String themeName) {

        if (themeName == null || themeName.isBlank()) {
            LOG.info("no theme given, falling back to Theme.{}.", SYSTEM);
            return SYSTEM;
        }

        try {
            return Theme.valueOf(themeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOG.info("tried to map unknown name={} to Theme, falling back to Theme.{}.", themeName, SYSTEM);
            return SYSTEM;
        }
    }
}
