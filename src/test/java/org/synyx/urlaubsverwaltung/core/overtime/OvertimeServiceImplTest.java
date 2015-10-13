package org.synyx.urlaubsverwaltung.core.overtime;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class OvertimeServiceImplTest {

    private OvertimeService overtimeService;

    private OvertimeDAO overtimeDAO;

    @Before
    public void setUp() {

        overtimeDAO = Mockito.mock(OvertimeDAO.class);
        overtimeService = new OvertimeServiceImpl(overtimeDAO);
    }


    @Test
    public void ensurePersistsOvertime() {

        Overtime overtime = Mockito.mock(Overtime.class);

        overtimeService.save(overtime);

        Mockito.verify(overtimeDAO).save(overtime);
    }
}
