package org.synyx.urlaubsverwaltung.holiday.api;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameter;
import de.jollyday.ManagerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

@Configuration
public class PublicHolidayConfiguration {

    private static final String HOLIDAY_DEFINITION_FILE = "Holidays_de.xml";

    @Bean
    public HolidayManager getHolidayManager() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(HOLIDAY_DEFINITION_FILE);
        ManagerParameter managerParameter = ManagerParameters.create(url);

        return HolidayManager.getInstance(managerParameter);
    }
}
