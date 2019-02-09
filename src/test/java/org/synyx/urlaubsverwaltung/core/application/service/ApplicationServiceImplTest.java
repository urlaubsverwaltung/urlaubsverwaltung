/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.application.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

import static org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus.WAITING;

import static java.util.Collections.singletonList;


/**
 * Unit test for {@link ApplicationServiceImpl}.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ApplicationServiceImplTest {

    private ApplicationService applicationService;
    private ApplicationDAO applicationDAO;

    @Before
    public void setUp() {

        applicationDAO = Mockito.mock(ApplicationDAO.class);
        applicationService = new ApplicationServiceImpl(applicationDAO);
    }


    // Get application by ID -------------------------------------------------------------------------------------------

    @Test
    public void ensureGetApplicationByIdCallsCorrectDaoMethod() {

        applicationService.getApplicationById(1234);
        Mockito.verify(applicationDAO).findOne(1234);
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
        Mockito.verify(applicationDAO).save(application);
    }


    // Get total overtime reduction ------------------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetTotalOvertimeReductionForNullPerson() {

        applicationService.getTotalOvertimeReductionOfPerson(null);
    }


    @Test
    public void ensureReturnsZeroIfPersonHasNoApplicationsForLeaveYet() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(applicationDAO.calculateTotalOvertimeOfPerson(person)).thenReturn(null);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        Mockito.verify(applicationDAO).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ZERO, totalHours);
    }


    @Test
    public void getForStates() {

        Application application = new Application();
        List<Application> applications = singletonList(application);

        Mockito.when(applicationDAO.findByStatusIn(singletonList(WAITING))).thenReturn(applications);

        List<Application> result = applicationService.getForStates(singletonList(WAITING));
        assertEquals(applications, result);
    }


    @Test
    public void ensureReturnsCorrectTotalOvertimeReductionForPerson() {

        Person person = TestDataCreator.createPerson();

        Mockito.when(applicationDAO.calculateTotalOvertimeOfPerson(person)).thenReturn(BigDecimal.ONE);

        BigDecimal totalHours = applicationService.getTotalOvertimeReductionOfPerson(person);

        Mockito.verify(applicationDAO).calculateTotalOvertimeOfPerson(person);

        Assert.assertNotNull("Should not be null", totalHours);
        assertEquals("Wrong total overtime reduction", BigDecimal.ONE, totalHours);
    }
}
