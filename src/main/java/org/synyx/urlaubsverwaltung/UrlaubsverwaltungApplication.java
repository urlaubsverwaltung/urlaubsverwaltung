package org.synyx.urlaubsverwaltung;

import de.focus_shift.launchpad.core.LaunchpadAutoConfiguration;
import de.focus_shift.launchpad.tenancy.LaunchpadTenantConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({LaunchpadTenantConfiguration.class, LaunchpadAutoConfiguration.class})
public class UrlaubsverwaltungApplication extends SpringBootServletInitializer {

    @Override
    @Deprecated
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(UrlaubsverwaltungApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(UrlaubsverwaltungApplication.class, args);
    }
}
