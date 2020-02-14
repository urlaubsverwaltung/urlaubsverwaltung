package org.synyx.urlaubsverwaltung.calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class CalendarAccessibleService {

    private final CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository;

    @Autowired
    CalendarAccessibleService(CompanyCalendarAccessibleRepository companyCalendarAccessibleRepository) {
        this.companyCalendarAccessibleRepository = companyCalendarAccessibleRepository;
    }

    public boolean isCompanyCalendarAccessible() {
        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();

        if (companyCalendarAccessibleList.isEmpty()) {
            return false;
        }

        return companyCalendarAccessibleList.get(0).isAccessible();
    }

    public void setCompanyCalendarAccessibility(boolean isCompanyCalendarAccessible) {

        final CompanyCalendarAccessible companyCalendarAccessible;

        final List<CompanyCalendarAccessible> companyCalendarAccessibleList = companyCalendarAccessibleRepository.findAll();
        if (companyCalendarAccessibleList.isEmpty()) {
            companyCalendarAccessible = new CompanyCalendarAccessible();
            companyCalendarAccessible.setAccessible(isCompanyCalendarAccessible);
        } else {
            companyCalendarAccessible = companyCalendarAccessibleList.get(0);
            companyCalendarAccessible.setAccessible(isCompanyCalendarAccessible);
        }

        companyCalendarAccessibleRepository.save(companyCalendarAccessible);
    }
}
