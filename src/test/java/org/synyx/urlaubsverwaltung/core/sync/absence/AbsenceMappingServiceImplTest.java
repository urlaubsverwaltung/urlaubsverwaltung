package org.synyx.urlaubsverwaltung.core.sync.absence;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;


/**
 * Daniel Hammann - <hammann@synyx.de>.
 */
public class AbsenceMappingServiceImplTest {

    private AbsenceMappingService sut;
    private AbsenceMappingDAO absenceMappingDAO;

    @Before
    public void setUp() throws Exception {

        absenceMappingDAO = Mockito.mock(AbsenceMappingDAO.class);
        sut = new AbsenceMappingServiceImpl(absenceMappingDAO);
    }


    @Test
    public void shouldCreateAbsenceMappingForVacation() throws Exception {

        String eventId = "eventId";

        AbsenceMapping result = sut.create(42, AbsenceType.VACATION, eventId);

        assertThat(result.getAbsenceId(), is(42));
        assertThat(result.getAbsenceType(), is(AbsenceType.VACATION));
        assertThat(result.getEventId(), is(eventId));
        Mockito.verify(absenceMappingDAO).save(result);
    }


    @Test
    public void shouldCreateAbsenceMappingForSickDay() throws Exception {

        String eventId = "eventId";

        AbsenceMapping result = sut.create(21, AbsenceType.SICKNOTE, eventId);

        assertThat(result.getAbsenceId(), is(21));
        assertThat(result.getAbsenceType(), is(AbsenceType.SICKNOTE));
        assertThat(result.getEventId(), is(eventId));
        Mockito.verify(absenceMappingDAO).save(result);
    }


    @Test
    public void shouldCallAbsenceMappingDaoDelete() {

        AbsenceMapping absenceMapping = new AbsenceMapping(42, AbsenceType.VACATION, "dummyEvent");
        sut.delete(absenceMapping);

        Mockito.verify(absenceMappingDAO).delete(absenceMapping);
    }


    @Test
    public void shouldCallAbsenceMappingDaoFind() {

        sut.getAbsenceByIdAndType(21, AbsenceType.SICKNOTE);

        Mockito.verify(absenceMappingDAO).findAbsenceMappingByAbsenceIdAndAbsenceType(21, AbsenceType.SICKNOTE);
    }
}
