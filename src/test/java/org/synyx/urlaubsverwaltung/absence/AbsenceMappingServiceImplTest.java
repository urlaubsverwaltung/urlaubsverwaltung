package org.synyx.urlaubsverwaltung.absence;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class AbsenceMappingServiceImplTest {

    private AbsenceMappingService sut;
    private AbsenceMappingRepository absenceMappingRepository;

    @Before
    public void setUp() {

        absenceMappingRepository = mock(AbsenceMappingRepository.class);
        sut = new AbsenceMappingServiceImpl(absenceMappingRepository);
    }


    @Test
    public void shouldCreateAbsenceMappingForVacation() {

        String eventId = "eventId";

        AbsenceMapping result = sut.create(42, AbsenceType.VACATION, eventId);

        assertThat(result.getAbsenceId(), is(42));
        assertThat(result.getAbsenceType(), is(AbsenceType.VACATION));
        assertThat(result.getEventId(), is(eventId));
        verify(absenceMappingRepository).save(result);
    }


    @Test
    public void shouldCreateAbsenceMappingForSickDay() {

        String eventId = "eventId";

        AbsenceMapping result = sut.create(21, AbsenceType.SICKNOTE, eventId);

        assertThat(result.getAbsenceId(), is(21));
        assertThat(result.getAbsenceType(), is(AbsenceType.SICKNOTE));
        assertThat(result.getEventId(), is(eventId));
        verify(absenceMappingRepository).save(result);
    }


    @Test
    public void shouldCallAbsenceMappingDaoDelete() {

        AbsenceMapping absenceMapping = new AbsenceMapping(42, AbsenceType.VACATION, "dummyEvent");
        sut.delete(absenceMapping);

        verify(absenceMappingRepository).delete(absenceMapping);
    }


    @Test
    public void shouldCallAbsenceMappingDaoFind() {

        sut.getAbsenceByIdAndType(21, AbsenceType.SICKNOTE);

        verify(absenceMappingRepository).findAbsenceMappingByAbsenceIdAndAbsenceType(21, AbsenceType.SICKNOTE);
    }
}
