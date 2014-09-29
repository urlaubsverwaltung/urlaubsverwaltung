/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.synyx.urlaubsverwaltung.core.application.service;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;


/**
 * Unit test for serivce {@link ApplicationServiceImpl}.
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


    @Test
    public void ensureGetApplicationByIdCallsCorrectDaoMethod() {

        applicationService.getApplicationById(1234);
        Mockito.verify(applicationDAO).findOne(1234);
    }


    @Test
    public void ensureSaveCallsCorrectDaoMethod() {

        Application application = new Application();

        applicationService.save(application);
        Mockito.verify(applicationDAO).save(application);
    }
}
