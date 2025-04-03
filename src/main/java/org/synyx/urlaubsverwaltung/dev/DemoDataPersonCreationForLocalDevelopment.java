package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.boot.availability.LivenessState;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.availability.LivenessState.CORRECT;

/**
 * This class creates person demo data for local development
 */
@ConditionalOnProperty(prefix = "uv.development.demodata", name = "local-development", havingValue = "true")
@ConditionalOnBean(PersonDataProvider.class)
@Component
public class DemoDataPersonCreationForLocalDevelopment {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    static final String EMAIL_USER = "user@urlaubsverwaltung.cloud";
    static final String EMAIL_DEPARTMENT_HEAD = "departmentHead@urlaubsverwaltung.cloud";
    static final String EMAIL_SECOND_STAGE_AUTHORITY = "secondStageAuthority@urlaubsverwaltung.cloud";
    static final String EMAIL_BOSS = "boss@urlaubsverwaltung.cloud";
    static final String EMAIL_OFFICE = "office@urlaubsverwaltung.cloud";
    static final String EMAIL_DAMPF = "dampf@urlaubsverwaltung.cloud";
    static final String EMAIL_SCHMIDT = "schmidt@urlaubsverwaltung.cloud";
    static final String EMAIL_HAENDEL = "haendel@urlaubsverwaltung.cloud";
    static final String EMAIL_HDIETER = "hdieter@urlaubsverwaltung.cloud";
    static final String EMAIL_SCHNEIDER = "schneider@urlaubsverwaltung.cloud";
    static final String EMAIL_BAIER = "baier@urlaubsverwaltung.cloud";
    static final String EMAIL_ADMIN = "admin@urlaubsverwaltung.cloud";

    private final PersonDataProvider personDataProvider;
    private final DemoDataProperties demoDataProperties;

    DemoDataPersonCreationForLocalDevelopment(PersonDataProvider personDataProvider, DemoDataProperties demoDataProperties) {
        this.personDataProvider = personDataProvider;
        this.demoDataProperties = demoDataProperties;
    }

    // AvailabilityChangeEvent is after ApplicationStartedEvent which creates sick note type definitions in database which are needed for demo data creations
    @EventListener(AvailabilityChangeEvent.class)
    public void onAvailabilityChange(AvailabilityChangeEvent<? extends AvailabilityState> event) {
        if (event.getState() instanceof LivenessState livenessState && CORRECT.equals(livenessState)) {
            createDemoPersons();
        }
    }

    private void createDemoPersons() {
        LOG.info("Creating demo persons for local development");
        personDataProvider.createTestPerson("user", "Klaus", "Müller", EMAIL_USER);
        personDataProvider.createTestPerson("departmentHead", "Thorsten", "Krüger", EMAIL_DEPARTMENT_HEAD);
        personDataProvider.createTestPerson("secondStageAuthority", "Juliane", "Huber", EMAIL_SECOND_STAGE_AUTHORITY);
        personDataProvider.createTestPerson("boss", "Theresa", "Scherer", EMAIL_BOSS);
        personDataProvider.createTestPerson("office", "Marlene", "Muster", EMAIL_OFFICE);
        personDataProvider.createTestPerson("admin", "Anne", "Roth", EMAIL_ADMIN);

        // Users
        personDataProvider.createTestPerson("hdampf", "Hans", "Dampf", EMAIL_DAMPF);
        personDataProvider.createTestPerson("fbaier", "Franziska", "Baier", EMAIL_BAIER);
        personDataProvider.createTestPerson("eschneider", "Elena", "Schneider", EMAIL_SCHNEIDER);
        personDataProvider.createTestPerson("bhaendel", "Brigitte", "Händel", EMAIL_HAENDEL);
        personDataProvider.createTestPerson("nschmidt", "Niko", "Schmidt", EMAIL_SCHMIDT);
        personDataProvider.createTestPerson("heinz", "Holger", "Dieter", EMAIL_HDIETER);
        IntStream.rangeClosed(0, demoDataProperties.getAdditionalActiveUser()).forEach(i -> personDataProvider.createTestPerson("horst-active-" + i, "Horst", "Aktiv", "hdieter-active-" + i + "@urlaubsverwaltung.cloud"));
        IntStream.rangeClosed(0, demoDataProperties.getAdditionalInactiveUser()).forEach(i -> personDataProvider.createTestPerson("horst-inactive-" + i, "Horst", "Inaktiv", "hdieter-inactive-" + i + "@urlaubsverwaltung.cloud"));
        LOG.info("Created demo persons for local development");
    }
}
