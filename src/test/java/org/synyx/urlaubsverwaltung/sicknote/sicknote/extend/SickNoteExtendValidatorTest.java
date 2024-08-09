package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SickNoteExtendValidatorTest {

    private SickNoteExtendValidator sut;

    @Mock
    private SickNoteService sickNoteService;
    @Mock
    private DateFormatAware dateFormatAware;

    @Mock
    private Errors errors;

    @BeforeEach
    void setUp() {
        sut = new SickNoteExtendValidator(sickNoteService, dateFormatAware);
    }

    @Test
    void ensureExceptionThrownWhenSickNoteIdIsNull() {

        final SickNoteExtendDto dto = new SickNoteExtendDto();

        assertThatThrownBy(() -> sut.validate(dto, errors))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot validate sickNoteExtension without sickNoteId.");
    }

    @Test
    void ensureExceptionThrownWhenSickNoteIdDoesNotExist() {

        when(sickNoteService.getById(1L)).thenReturn(Optional.empty());

        final SickNoteExtendDto dto = new SickNoteExtendDto();
        dto.setSickNoteId(1L);

        assertThatThrownBy(() -> sut.validate(dto, errors))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot validate sickNoteExtension with sickNoteId=1 which does not exist.");
    }

    static Stream<Arguments> endDateTuples() {
        return Stream.of(
            Arguments.of(LocalDate.of(2024, 8, 8), LocalDate.of(2024, 8, 7)),
            Arguments.of(LocalDate.of(2024, 8, 8), LocalDate.of(2024, 8, 8))
        );
    }

    @ParameterizedTest
    @MethodSource("endDateTuples")
    void ensureEndDateMustBeAfterCurrentDate(LocalDate sickNoteEndDate, LocalDate nextEndDate) {

        final SickNote sickNote = SickNote.builder().id(1L).endDate(sickNoteEndDate).build();
        when(sickNoteService.getById(1L)).thenReturn(Optional.of(sickNote));

        when(dateFormatAware.format(sickNoteEndDate)).thenReturn("<any-formatted-date>");

        final SickNoteExtendDto dto = new SickNoteExtendDto();
        dto.setSickNoteId(1L);
        dto.setEndDate(nextEndDate);

        sut.validate(dto, errors);

        verify(errors).rejectValue("endDate", "sicknote.extend.validation.constraints.end-date.future.message",  new Object[]{"<any-formatted-date>"}, "");
        verify(errors).rejectValue("extendToDate", "sicknote.extend.validation.constraints.end-date.future.message",  new Object[]{"<any-formatted-date>"}, "");
    }
}
