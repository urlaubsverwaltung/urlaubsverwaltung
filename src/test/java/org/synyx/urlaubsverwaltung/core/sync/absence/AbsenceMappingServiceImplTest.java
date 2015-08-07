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
    public void shouldCreateAbsenceMappingByApplicationForLeave() throws Exception {

        Application application = Mockito.mock(Application.class);
        Mockito.when(application.getId()).thenReturn(42);

        String eventId = "eventId";

        AbsenceMapping result = sut.create(application, eventId);

        assertThat(result.getAbsenceId(), is(42));
        assertThat(result.getAbsenceType(), is(AbsenceType.VACATION));
        assertThat(result.getEventId(), is(eventId));
        Mockito.verify(absenceMappingDAO).save(result);
    }


    @Test
    public void shouldCreateAbsenceMappingBySicknote() throws Exception {

        SickNote sicknote = Mockito.mock(SickNote.class);
        Mockito.when(sicknote.getId()).thenReturn(21);

        String eventId = "eventId";

        AbsenceMapping result = sut.create(sicknote, eventId);

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

        Optional<AbsenceMapping> absenceMapping = sut.getAbsenceByIdAndType(21, AbsenceType.SICKNOTE);

        Mockito.verify(absenceMappingDAO).findAbsenceMappingByAbsenceIdAndAbsenceType(21, AbsenceType.SICKNOTE);
    }
}
