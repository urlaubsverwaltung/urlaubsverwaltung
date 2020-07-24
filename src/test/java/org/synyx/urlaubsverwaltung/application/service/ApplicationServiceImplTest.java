package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationRepository;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.demodatacreator.DemoDataCreator;
import org.synyx.urlaubsverwaltung.person.Person;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Unit test for {@link ApplicationServiceImpl}.
 */
class ApplicationServiceImplTest {

    private ApplicationService applicationService;
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void setUp() {

        applicationRepository = mock(ApplicationRepository.class);
        applicationService = new ApplicationServiceImpl(applicationRepository);
    }


    // Get application by ID -------------------------------------------------------------------------------------------

    @Test
    void ensureGetApplicationByIdCallsCorrectDaoMethod() {

        applicationService.getApplicationById(1234);
        verify(applicationRepository).findById(1234);
    }


    @Test
    void ensureGetApplicationByIdReturnsAbsentOptionalIfNoOneExists() {

        Optional<Application> optional = applicationService.getApplicationById(1234);

        Assert.assertNotNull("Optional must not be null", optional);
        Assert.assertFalse("No application for leave should exist", optional.isPresent());
    }


    // Save application ------------------------------------------------------------------------------------------------

    @Test
    void ensureSaveCallsCorrectDaoMethod() {

        Application application = new Application();

        applicationService.save(application);
        verify(applicationRepository).save(application);
    }


    // Get total overtime reduction ------------------------------------------------------------------------------------

    @Test
    void ensureThrowsIfTryingToGetTotalOvertimeReductionForNullPerson() {

        assertThatIllegalArgumentException().isThrownBy(() -> applicationService.getTotalOvertimeReductionOfPerson(null));
    }


    @Test
    void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

        Person person = DemoDataCreator.createPerson();

        when(applicationRepository.calculateTotalOvertimeOfPerson(person)).thenReturn(null);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ZERO, totalHours);
    }

    @Test
    void getForStates() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        when(applicationRepository.findByStatusIn(List.of(WAITING))).thenReturn(applications);

        final List<Application> result = applicationService.getForStates(List.of(WAITING));
        assertEquals(applications, result);
    }

    @Test
    void getForStatesAndPerson() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        final Person person = DemoDataCreator.createPerson();

        when(applicationRepository.findByStatusInAndPersonIn(List.of(WAITING), List.of(person))).thenReturn(applications);

        final List<Application> result = applicationService.getForStatesAndPerson(List.of(WAITING), List.of(person));
        assertEquals(applications, result);
    }


    @Test
    void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

        Person person = DemoDataCreator.createPerson();

        when(applicationRepository.calculateTotalOvertimeOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        verify(applicationRepository).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ONE, totalHours);
    }
}
