package org.synyx.urlaubsverwaltung.workingtime.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties("uv.workingtime")
public class WorkingTimeProperties {

    @NotNull
    private List<Integer> defaultWorkingDays;

    public List<Integer> getDefaultWorkingDays() {
        return defaultWorkingDays;
    }

    public void setDefaultWorkingDays(List<Integer> defaultWorkingDays) {
        this.defaultWorkingDays = defaultWorkingDays;
    }
}
