package org.synyx.urlaubsverwaltung.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.IntStream;

/**
 * This class creates person demo data for local development
 */
@ConditionalOnProperty(prefix = "uv.development.demodata", name = "localDevelopment", havingValue = "true")
@ConditionalOnBean(PersonDataProvider.class)
@Component
public class DemoDataPersonCreationForLocalDevelopment {

    private static final Logger LOG = LoggerFactory.getLogger(DemoDataPersonCreationForLocalDevelopment.class);

    private final PersonDataProvider personDataProvider;
    private final DemoDataProperties demoDataProperties;

    public DemoDataPersonCreationForLocalDevelopment(PersonDataProvider personDataProvider, DemoDataProperties demoDataProperties) {
        this.personDataProvider = personDataProvider;
        this.demoDataProperties = demoDataProperties;
    }

    @Async
    @EventListener(ApplicationStartedEvent.class)
    public void createDemoPersons() {
        LOG.info("creating demo persons for local development");
        personDataProvider.createTestPerson("user", "Klaus", "Müller", "user@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("departmentHead", "Thorsten", "Krüger", "departmentHead@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("secondStageAuthority", "Juliane", "Huber", "secondStageAuthority@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("boss", "Theresa", "Scherer", "boss@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("office", "Marlene", "Muster", "office@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("admin", "Anne", "Roth", "admin@urlaubsverwaltung.cloud");

        // Users
        personDataProvider.createTestPerson("hdampf", "Hans", "Dampf", "dampf@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("fbaier", "Franziska", "Baier", "baier@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("eschneider", "Elena", "Schneider", "schneider@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("bhaendel", "Brigitte", "Händel", "haendel@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("nschmidt", "Niko", "Schmidt", "schmidt@urlaubsverwaltung.cloud");
        personDataProvider.createTestPerson("heinz", "Holger", "Dieter", "hdieter@urlaubsverwaltung.cloud");
        IntStream.rangeClosed(0, demoDataProperties.getAdditionalActiveUser()).forEach(i -> personDataProvider.createTestPerson("horst-active-" + i, "Horst", "Aktiv", "hdieter-active@urlaubsverwaltung.cloud"));
        IntStream.rangeClosed(0, demoDataProperties.getAdditionalInactiveUser()).forEach(i -> personDataProvider.createTestPerson("horst-inactive-" + i, "Horst", "Inaktiv", "hdieter-inactive@urlaubsverwaltung.cloud"));
        LOG.info("created demo persons for local development");
    }
}
