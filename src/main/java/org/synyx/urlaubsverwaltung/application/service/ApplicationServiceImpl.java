package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;

/**
 * Implementation of interface {@link ApplicationService}.
 */
@Service
class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    ApplicationServiceImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Optional<Application> getApplicationById(Integer id) {
        return applicationRepository.findById(id);
    }

    @Override
    public Application save(Application application) {
        return applicationRepository.save(application);
    }

    @Override

    public List<Application> getApplicationsForACertainPeriodAndPerson(LocalDate startDate, LocalDate endDate, Person person) {
        return applicationRepository.getApplicationsForACertainTimeAndPerson(startDate, endDate, person);
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndState(LocalDate startDate, LocalDate endDate, ApplicationStatus status) {
        return applicationRepository.getApplicationsForACertainTimeAndState(startDate, endDate, status);
    }

    @Override
    public List<Application> getApplicationsForACertainPeriodAndPersonAndState(LocalDate startDate, LocalDate endDate, Person person, ApplicationStatus status) {
        return applicationRepository.getApplicationsForACertainTimeAndPersonAndState(startDate, endDate, person, status);
    }

    @Override
    public List<Application> getForStates(List<ApplicationStatus> statuses) {
        return applicationRepository.findByStatusIn(statuses);
    }

    @Override
    public List<Application> getForStatesSince(List<ApplicationStatus> statuses, LocalDate since) {
        return applicationRepository.findByStatusInAndEndDateGreaterThanEqual(statuses, since);
    }

    @Override
    public List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons) {
        return applicationRepository.findByStatusInAndPersonIn(statuses, persons);
    }

    @Override
    public List<Application> getForStatesAndPersonSince(List<ApplicationStatus> statuses, List<Person> persons, LocalDate since) {
        return applicationRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(statuses, persons, since);
    }

    @Override
    public Duration getTotalOvertimeReductionOfPerson(Person person) {
        final BigDecimal overtimeReduction = Optional.ofNullable(applicationRepository.calculateTotalOvertimeReductionOfPerson(person)).orElse(BigDecimal.ZERO);
        return Duration.ofMinutes(overtimeReduction.multiply(BigDecimal.valueOf(60)).longValue());
    }

    @Override
    public List<Application> getForHolidayReplacement(Person holidayReplacement, LocalDate date) {
        final List<ApplicationStatus> statuses = List.of(WAITING, TEMPORARY_ALLOWED, ALLOWED, ALLOWED_CANCELLATION_REQUESTED);
        return applicationRepository.findByHolidayReplacementAndEndDateIsGreaterThanEqualAndStatusIn(holidayReplacement, date, statuses);
    }
}
