package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Role;

import java.util.List;

@Service
class CalendarAccessibleService {

    private final CompanyCalendarService companyCalendarService;
    private final CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository;

    @Autowired
    CalendarAccessibleService(CompanyCalendarService companyCalendarService,
                              CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository) {

        this.companyCalendarService = companyCalendarService;
        this.companyCalendarAccessibleRepository = companyCalendarAccessibleRepository;
    }

    boolean isCompanyCalendarAccessible() {
        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();

        if (companyCalendarAccessibleList.isEmpty()) {
            return false;
        }

        return companyCalendarAccessibleList.get(0).isAccessible();
    }

    void enableCompanyCalendar() {
        setCompanyCalendarAccessibility(true);
    }

    void disableCompanyCalendar() {
        setCompanyCalendarAccessibility(false);
        companyCalendarService.deleteCalendarsForPersonsWithoutOneOfRole(Role.BOSS, Role.OFFICE);
    }

    private void setCompanyCalendarAccessibility(boolean isCompanyCalendarAccessible) {

        final CompanyCalendarAccessible companyCalendarAccessible;

        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();
        if (companyCalendarAccessibleList.isEmpty()) {
            companyCalendarAccessible = new CompanyCalendarAccessible();
        } else {
            companyCalendarAccessible = companyCalendarAccessibleList.get(0);
        }
        companyCalendarAccessible.setAccessible(isCompanyCalendarAccessible);

        companyCalendarAccessibleRepository.save(companyCalendarAccessible);
    }
}
