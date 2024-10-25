package org.synyx.urlaubsverwaltung.publicholiday;

import de.focus_shift.jollyday.core.HolidayManager;
import de.focus_shift.jollyday.core.ManagerParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Configuration
class PublicHolidayConfiguration {

    private static final List<String> COUNTRIES = List.of("de", "at", "ch", "gb", "gr", "mt", "it", "hr", "es", "nl", "lt", "be", "pl");

    @Bean
    Map<String, HolidayManager> holidayManagerMap() {
        return COUNTRIES.stream()
            .map(country -> HolidayManager.getInstance(ManagerParameters.create(country)))
            .collect(toMap(holidayManager -> holidayManager.getManagerParameter().getDisplayName(), Function.identity()));
    }
}
