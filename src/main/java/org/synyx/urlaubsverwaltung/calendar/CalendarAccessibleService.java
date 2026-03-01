package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

@Service
public class CalendarAccessibleService {

    private final CompanyCalendarService companyCalendarService;
    private final CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    CalendarAccessibleService(CompanyCalendarService companyCalendarService,
                              CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository,
                              ApplicationEventPublisher applicationEventPublisher) {

        this.companyCalendarService = companyCalendarService;
        this.companyCalendarAccessibleRepository = companyCalendarAccessibleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public boolean isCompanyCalendarAccessible() {
        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();

        if (companyCalendarAccessibleList.isEmpty()) {
            return false;
        }

        return companyCalendarAccessibleList.getFirst().isAccessible();
    }

    void enableCompanyCalendar() {
        setCompanyCalendarAccessibility(true);
        publishCompanyCalendarEnabledEvent();
    }

    void disableCompanyCalendar() {
        setCompanyCalendarAccessibility(false);
        companyCalendarService.deleteCalendarsForPersonsWithoutOneOfRole(Role.BOSS, Role.OFFICE);
        publishCompanyCalendarDisabledEvent();
    }

    private void publishCompanyCalendarEnabledEvent() {
        applicationEventPublisher.publishEvent(CompanyCalendarEnabledEvent.of());
    }

    private void publishCompanyCalendarDisabledEvent() {
        applicationEventPublisher.publishEvent(CompanyCalendarDisabledEvent.of());
    }

    private void setCompanyCalendarAccessibility(boolean isCompanyCalendarAccessible) {

        final CompanyCalendarAccessible companyCalendarAccessible;

        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();
        if (companyCalendarAccessibleList.isEmpty()) {
            companyCalendarAccessible = new CompanyCalendarAccessible();
        } else {
            companyCalendarAccessible = companyCalendarAccessibleList.getFirst();
        }
        companyCalendarAccessible.setAccessible(isCompanyCalendarAccessible);

        companyCalendarAccessibleRepository.save(companyCalendarAccessible);
    }
}
