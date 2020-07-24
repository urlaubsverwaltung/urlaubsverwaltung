package org.synyx.urlaubsverwaltung.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


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
    public List<Application> getApplicationsForACertainState(ApplicationStatus state) {

        return applicationRepository.getApplicationsForACertainState(state);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndPerson(LocalDate startDate, LocalDate endDate,
                                                                       Person person) {

        return applicationRepository.getApplicationsForACertainTimeAndPerson(startDate, endDate, person);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndState(LocalDate startDate, LocalDate endDate,
                                                                      ApplicationStatus status) {

        return applicationRepository.getApplicationsForACertainTimeAndState(startDate, endDate, status);
    }


    @Override
    public List<Application> getApplicationsForACertainPeriodAndPersonAndState(LocalDate startDate,
                                                                               LocalDate endDate, Person person, ApplicationStatus status) {

        return applicationRepository.getApplicationsForACertainTimeAndPersonAndState(startDate, endDate, person, status);
    }

    @Override
    public List<Application> getForStates(List<ApplicationStatus> statuses) {

        return applicationRepository.findByStatusIn(statuses);
    }

    @Override
    public List<Application> getForStatesAndPerson(List<ApplicationStatus> statuses, List<Person> persons) {

        return applicationRepository.findByStatusInAndPersonIn(statuses, persons);
    }


    @Override
    public BigDecimal getTotalOvertimeReductionOfPerson(Person person) {

        Assert.notNull(person, "Person to get overtime reduction for must be given.");

        return Optional.ofNullable(applicationRepository.calculateTotalOvertimeOfPerson(person))
            .orElse(BigDecimal.ZERO);
    }
}
