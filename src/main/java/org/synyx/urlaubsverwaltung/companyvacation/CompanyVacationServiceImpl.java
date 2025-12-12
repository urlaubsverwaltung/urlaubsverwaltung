package org.synyx.urlaubsverwaltung.companyvacation;

import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.publicholiday.PublicHolidaysSettings;
import org.synyx.urlaubsverwaltung.settings.SettingsService;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForChristmasEveUpdatedEvent;
import org.synyx.urlaubsverwaltung.settings.WorkingDurationForNewYearsEveUpdatedEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.lookup;
import static java.time.Month.DECEMBER;
import static org.slf4j.LoggerFactory.getLogger;

@Service
class CompanyVacationServiceImpl implements CompanyVacationService {

    private static final Logger LOG = getLogger(lookup().lookupClass());

    // In first place we do not have another source then public holiday settings
    // later with fully implemented company vacation management this might be used by a source id like source id of
    // application or sick note to determine origin
    private static final String SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE = "settings-christmas-eve";
    private static final String SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE = "settings-new-years-eve";

    private final SettingsService settingsService;
    private final Clock clock;
    private final ApplicationEventPublisher applicationEventPublisher;

    CompanyVacationServiceImpl(
        SettingsService settingsService,
        Clock clock,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.settingsService = settingsService;
        this.clock = clock;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @EventListener
    void handleWorkingDurationForChristmasEveUpdatedEvent(WorkingDurationForChristmasEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForChristmasEveUpdatedEvent {}", event);
        convertChristmasEveToCompanyVacationEvent(event.workingDurationForChristmasEve());
    }

    @EventListener
    void handleWorkingDurationForNewYearsEveUpdatedEvent(WorkingDurationForNewYearsEveUpdatedEvent event) {
        LOG.info("Received WorkingDurationForNewYearsEveUpdatedEvent {}", event);
        convertNewYearsEveToCompanyVacationEvent(event.workingDurationForNewYearsEve());
    }

    public void publishCompanyEvents() {
        LOG.info("Republishing all company vacation events based on public holiday settings (working duration Christmas and new years eve)");

        final PublicHolidaysSettings publicHolidaysSettings = settingsService.getSettings().getPublicHolidaysSettings();
        convertChristmasEveToCompanyVacationEvent(publicHolidaysSettings.getWorkingDurationForChristmasEve());
        convertNewYearsEveToCompanyVacationEvent(publicHolidaysSettings.getWorkingDurationForNewYearsEve());

        LOG.info("Republished all company vacation events based on public holiday settings (working duration Christmas and new years eve)");
    }

    private void convertNewYearsEveToCompanyVacationEvent(DayLength workingDurationForNewYearsEve) {
        convertAndPublishCompanyEvent(workingDurationForNewYearsEve, DECEMBER, 31, SOURCE_ID_FOR_SETTINGS_NEW_YEARS_EVE);
    }

    private void convertChristmasEveToCompanyVacationEvent(DayLength workingDurationForChristmasEve) {
        convertAndPublishCompanyEvent(workingDurationForChristmasEve, DECEMBER, 24, SOURCE_ID_FOR_SETTINGS_CHRISTMAS_EVE);
    }

    private void convertAndPublishCompanyEvent(DayLength dayLength, Month month, int dayOfMonth, String sourceId) {

        final DayLength companyVacationLength = dayLength.getInverse();
        final LocalDate date = LocalDate.of(LocalDate.now(clock).getYear(), month, dayOfMonth);

        if (!companyVacationLength.isZero()) {
            final CompanyVacationPublishedEvent companyVacationPublishedEvent = new CompanyVacationPublishedEvent(sourceId, UUID.randomUUID(), Instant.now(), companyVacationLength, date, date);
            applicationEventPublisher.publishEvent(companyVacationPublishedEvent);
            LOG.info("Published CompanyVacationPublishedEvent {}", companyVacationPublishedEvent);
        } else {
            final CompanyVacationDeletedEvent companyVacationDeletedEvent = new CompanyVacationDeletedEvent(sourceId, UUID.randomUUID(), Instant.now());
            applicationEventPublisher.publishEvent(companyVacationDeletedEvent);
            LOG.info("Published CompanyVacationDeletedEvent {}", companyVacationDeletedEvent);
        }
    }
}
