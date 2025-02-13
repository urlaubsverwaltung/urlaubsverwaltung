package org.synyx.urlaubsverwaltung;

import de.focus_shift.launchpad.core.LaunchpadAutoConfiguration;
import de.focus_shift.launchpad.tenancy.LaunchpadTenantConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Import({LaunchpadTenantConfiguration.class, LaunchpadAutoConfiguration.class})
public class UrlaubsverwaltungApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
