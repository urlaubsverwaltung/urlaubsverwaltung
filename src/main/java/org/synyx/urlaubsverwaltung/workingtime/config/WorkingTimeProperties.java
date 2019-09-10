package org.synyx.urlaubsverwaltung.workingtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

import static java.util.Collections.emptyList;

@Component
@ConfigurationProperties("uv.workingtime")
@Validated
public class WorkingTimeProperties {

    @NotNull
    private List<@Min(1) @Max(7) Integer> defaultWorkingDays = emptyList();

    public List<Integer> getDefaultWorkingDays() {
        return defaultWorkingDays;
    }

    public void setDefaultWorkingDays(List<Integer> defaultWorkingDays) {
        this.defaultWorkingDays = defaultWorkingDays;
    }
}
