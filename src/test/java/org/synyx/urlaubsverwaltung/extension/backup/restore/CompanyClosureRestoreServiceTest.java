package org.synyx.urlaubsverwaltung.extension.backup.restore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureEntity;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureImportService;
import org.synyx.urlaubsverwaltung.companyclosure.CompanyClosureVersionEntity;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.CompanyClosureVersionDTO;
import org.synyx.urlaubsverwaltung.extension.backup.model.DayLengthDTO;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.time.Month.JANUARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyClosureRestoreServiceTest {

    private CompanyClosureRestoreService sut;

    @Mock
    private CompanyClosureImportService companyClosureImportService;
    @Mock
    private PersonService personService;

    @Captor
    private ArgumentCaptor<CompanyClosureEntity> closureCaptor;
    @Captor
    private ArgumentCaptor<List<CompanyClosureVersionEntity>> versionsCaptor;

    @BeforeEach
    void setUp() {
        sut = new CompanyClosureRestoreService(companyClosureImportService, personService);
    }

    @Test
    void restoresCompanyClosures() {
        final CompanyClosureDTO christmasEve = new CompanyClosureDTO(12, 24, "companyclosure.name.christmasEve", null, null, null, List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.NOON, null, null, null),
            new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1), null, null)));
        final CompanyClosureDTO newYearsEve = new CompanyClosureDTO(12, 31, "companyclosure.name.newYearsEve", "Jahresabschluss", null, null, List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.MORNING, null, null, null)));

        sut.restore(List.of(christmasEve, newYearsEve));

        final InOrder inOrder = inOrder(companyClosureImportService);
        inOrder.verify(companyClosureImportService).deleteAll();
        inOrder.verify(companyClosureImportService, times(2)).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());

        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getMessageKey).containsExactly("companyclosure.name.christmasEve", "companyclosure.name.newYearsEve");
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getName).containsExactly(null, "Jahresabschluss");
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getDayOfMonth).containsExactly(24, 31);
        assertThat(closureCaptor.getAllValues()).extracting(CompanyClosureEntity::getMonth).containsExactly(12, 12);
        assertThat(versionsCaptor.getAllValues().get(0))
            .extracting(CompanyClosureVersionEntity::getDayLength, CompanyClosureVersionEntity::getValidFrom)
            .containsExactly(
                tuple(DayLength.NOON, null),
                tuple(DayLength.FULL, LocalDate.of(2027, JANUARY, 1)));
        assertThat(versionsCaptor.getAllValues().get(1))
            .extracting(CompanyClosureVersionEntity::getDayLength)
            .containsExactly(DayLength.MORNING);
    }

    @Test
    void restoresLastModifier() {
        final Person office = new Person("office", "Muster", "Marlene", "muster@example.org");
        when(personService.getPersonByUsername("office")).thenReturn(Optional.of(office));
        final Instant lastModifiedAt = Instant.parse("2026-11-06T10:00:00Z");
        final CompanyClosureDTO christmasEve = new CompanyClosureDTO(12, 24, "companyclosure.name.christmasEve", null, lastModifiedAt, "office", List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1), lastModifiedAt, "office")));

        sut.restore(List.of(christmasEve));

        verify(companyClosureImportService).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());
        assertThat(closureCaptor.getValue())
            .extracting(CompanyClosureEntity::getLastModifiedAt, CompanyClosureEntity::getLastModifiedBy)
            .containsExactly(lastModifiedAt, office);
        assertThat(versionsCaptor.getValue())
            .extracting(CompanyClosureVersionEntity::getLastModifiedAt, CompanyClosureVersionEntity::getLastModifiedBy)
            .containsExactly(tuple(lastModifiedAt, office));
    }

    @Test
    void restoresWithoutLastModifierWhenThePersonIsMissing() {
        when(personService.getPersonByUsername("deleted")).thenReturn(Optional.empty());
        final Instant lastModifiedAt = Instant.parse("2026-11-06T10:00:00Z");
        final CompanyClosureDTO christmasEve = new CompanyClosureDTO(12, 24, "companyclosure.name.christmasEve", null, null, null, List.of(
            new CompanyClosureVersionDTO(DayLengthDTO.FULL, LocalDate.of(2027, JANUARY, 1), lastModifiedAt, "deleted")));

        sut.restore(List.of(christmasEve));

        verify(companyClosureImportService).importCompanyClosure(closureCaptor.capture(), versionsCaptor.capture());
        assertThat(versionsCaptor.getValue())
            .extracting(CompanyClosureVersionEntity::getLastModifiedAt, CompanyClosureVersionEntity::getLastModifiedBy)
            .containsExactly(tuple(lastModifiedAt, null));
    }
}
