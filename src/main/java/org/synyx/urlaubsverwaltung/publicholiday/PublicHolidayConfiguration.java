package org.synyx.urlaubsverwaltung.publicholiday;

import de.jollyday.HolidayManager;
import de.jollyday.ManagerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
class PublicHolidayConfiguration {

    private static final List<String> COUNTRIES = List.of("de", "at");

    @Bean
    Map<String, HolidayManager> holidayManagerMap() {
        final Map<String, HolidayManager> countryMap = new HashMap<>();
        COUNTRIES.forEach(country -> {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            final URL url = cl.getResource("Holidays_" + country + ".xml");
            countryMap.put(country, HolidayManager.getInstance(ManagerParameters.create(url)));
        });
        return countryMap;
    }
}
