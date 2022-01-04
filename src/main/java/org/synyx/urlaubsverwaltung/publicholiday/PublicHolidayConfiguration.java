package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.HolidayManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

import static de.jollyday.ManagerParameters.create;

@Configuration
class PublicHolidayConfiguration {

    private static final String HOLIDAY_DEFINITION_FILE = "Holidays_de.xml";

    @Bean
    HolidayManager getHolidayManager() {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final URL url = cl.getResource(HOLIDAY_DEFINITION_FILE);
        return HolidayManager.getInstance(create(url));
    }
}
