package org.synyx.urlaubsverwaltung.application.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.synyx.urlaubsverwaltung.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.application.domain.Application;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.application.domain.ApplicationStatus.WAITING;


/**
 * Unit test for {@link ApplicationServiceImpl}.
 */
public class ApplicationServiceImplTest {

    private ApplicationService applicationService;
    private ApplicationDAO applicationDAO;

    @Before
    public void setUp() {

        applicationDAO = mock(ApplicationDAO.class);
        applicationService = new ApplicationServiceImpl(applicationDAO);
    }


    // Get application by ID -------------------------------------------------------------------------------------------

    @Test
    public void ensureGetApplicationByIdCallsCorrectDaoMethod() {

        applicationService.getApplicationById(1234);
        verify(applicationDAO).findById(1234);
    }


    @Test
    public void ensureGetApplicationByIdReturnsAbsentOptionalIfNoOneExists() {

        Optional<Application> optional = applicationService.getApplicationById(1234);

        Assert.assertNotNull("Optional must not be null", optional);
        Assert.assertFalse("No application for leave should exist", optional.isPresent());
    }


    // Save application ------------------------------------------------------------------------------------------------

    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        Application application = new Application();

        applicationService.save(application);
        verify(applicationDAO).save(application);
    }


    // Get total overtime reduction ------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetTotalOvertimeReductionForNullPerson() {

        applicationService.getTotalOvertimeReductionOfPerson(null);
    }


    @Test
    public void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

        Person person = TestDataCreator.createPerson();

        when(applicationDAO.calculateTotalOvertimeOfPerson(person)).thenReturn(null);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        verify(applicationDAO).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void getForStatesAndPerson() {

        final Application application = new Application();
        final List<Application> applications = List.of(application);

        final Person person = TestDataCreator.createPerson();

        when(applicationDAO.findByStatusInAndPersonIn(List.of(WAITING), List.of(person))).thenReturn(applications);

        final List<Application> result = applicationService.getForStatesAndPerson(List.of(WAITING), List.of(person));
        assertEquals(applications, result);
    }


    @Test
    public void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

        Person person = TestDataCreator.createPerson();

        when(applicationDAO.calculateTotalOvertimeOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        verify(applicationDAO).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ONE, totalHours);
    }
}
