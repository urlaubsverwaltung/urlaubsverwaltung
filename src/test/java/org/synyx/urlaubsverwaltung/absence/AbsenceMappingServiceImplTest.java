package org.synyx.urlaubsverwaltung.absence;

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
import static org.synyx.urlaubsverwaltung.absence.AbsenceType.SICKNOTE;
import static org.synyx.urlaubsverwaltung.absence.AbsenceType.VACATION;

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
        assertThat(absenceMapping.getAbsenceType()).isEqualTo(VACATION);
        assertThat(absenceMapping.getEventId()).isEqualTo(eventId);
    }

    @Test
    void shouldCreateAbsenceMappingForSickDay() {

        when(absenceMappingRepository.save(any(AbsenceMapping.class))).then(returnsFirstArg());

        final String eventId = "eventId";
        final AbsenceMapping absenceMapping = sut.create(21, SICKNOTE, eventId);
        assertThat(absenceMapping.getAbsenceId()).isEqualTo(21);
        assertThat(absenceMapping.getAbsenceType()).isEqualTo(SICKNOTE);
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

        verify(absenceMappingRepository).findAbsenceMappingByAbsenceIdAndAbsenceType(21, SICKNOTE);
    }
}
