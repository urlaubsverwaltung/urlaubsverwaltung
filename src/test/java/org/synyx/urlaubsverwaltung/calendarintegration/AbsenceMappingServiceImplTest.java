package org.synyx.urlaubsverwaltung.calendarintegration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.calendarintegration.AbsenceMappingType.VACATION;

@ExtendWith(MockitoExtension.class)
class AbsenceMappingServiceImplTest {

    private AbsenceMappingServiceImpl sut;

    @Mock
    private AbsenceMappingRepository absenceMappingRepository;

    @BeforeEach
    void setUp() {
        sut = new AbsenceMappingServiceImpl(absenceMappingRepository);
    }

    @Test
    void shouldCreateAbsenceMappingForVacation() {

        when(absenceMappingRepository.save(any(AbsenceMapping.class))).then(returnsFirstArg());

        final String eventId = "eventId";
        final AbsenceMapping absenceMapping = sut.create(42, VACATION, eventId);
        assertThat(absenceMapping.getAbsenceId()).isEqualTo(42);
        assertThat(absenceMapping.getAbsenceMappingType()).isEqualTo(VACATION);
        assertThat(absenceMapping.getEventId()).isEqualTo(eventId);
    }

    @Test
    void shouldCreateAbsenceMappingForSickDay() {

        when(absenceMappingRepository.save(any(AbsenceMapping.class))).then(returnsFirstArg());

        final String eventId = "eventId";
        final AbsenceMapping absenceMapping = sut.create(21, SICKNOTE, eventId);
        assertThat(absenceMapping.getAbsenceId()).isEqualTo(21);
        assertThat(absenceMapping.getAbsenceMappingType()).isEqualTo(SICKNOTE);
        assertThat(absenceMapping.getEventId()).isEqualTo(eventId);
    }

    @Test
    void shouldCallAbsenceMappingDaoDelete() {

        final AbsenceMapping absenceMapping = new AbsenceMapping(42, VACATION, "dummyEvent");
        sut.delete(absenceMapping);

        verify(absenceMappingRepository).delete(absenceMapping);
    }

    @Test
    void shouldCallAbsenceMappingDaoFind() {

        sut.getAbsenceByIdAndType(21, SICKNOTE);

        verify(absenceMappingRepository).findAbsenceMappingByAbsenceIdAndAbsenceMappingType(21, SICKNOTE);
    }

    @Test
    void equals() {
        final AbsenceMapping absenceMappingOne = new AbsenceMapping();
        absenceMappingOne.setId(1);

        final AbsenceMapping absenceMappingOneOne = new AbsenceMapping();
        absenceMappingOneOne.setId(1);

        final AbsenceMapping absenceMappingTwo = new AbsenceMapping();
        absenceMappingTwo.setId(2);

        assertThat(absenceMappingOne)
            .isEqualTo(absenceMappingOne)
            .isEqualTo(absenceMappingOneOne)
            .isNotEqualTo(absenceMappingTwo)
            .isNotEqualTo(new Object())
            .isNotEqualTo(null);
    }

    @Test
    void hashCodeTest() {
        final AbsenceMapping absenceMappingOne = new AbsenceMapping();
        absenceMappingOne.setId(1);

        assertThat(absenceMappingOne.hashCode()).isEqualTo(32);
    }
}
