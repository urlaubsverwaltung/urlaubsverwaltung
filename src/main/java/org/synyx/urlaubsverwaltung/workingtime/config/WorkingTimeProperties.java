package org.synyx.urlaubsverwaltung.workingtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties("uv.workingtime")
@Validated
public class WorkingTimeProperties {

    /**
     * Define the default working days that will be configured for
     * every newly created person.
     *
     * Default values: Monday, Tuesday, Wednesday, Thursday, Friday
     */
    @NotNull
    private List<@Min(1) @Max(7) Integer> defaultWorkingDays = List.of(1, 2, 3, 4, 5);

    public List<Integer> getDefaultWorkingDays() {
        return defaultWorkingDays;
    }

    public void setDefaultWorkingDays(List<Integer> defaultWorkingDays) {
        this.defaultWorkingDays = defaultWorkingDays;
    }
}
