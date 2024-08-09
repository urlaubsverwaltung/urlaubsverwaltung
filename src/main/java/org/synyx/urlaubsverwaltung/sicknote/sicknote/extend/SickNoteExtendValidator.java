package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;

import java.time.LocalDate;

@Component
class SickNoteExtendValidator implements Validator {

    private static final String ERROR_ENDDATE_FUTURE = "sicknote.extend.validation.constraints.end-date.future.message";

    private final SickNoteService sickNoteService;
    private final DateFormatAware dateFormatAware;

    SickNoteExtendValidator(SickNoteService sickNoteService, DateFormatAware dateFormatAware) {
        this.sickNoteService = sickNoteService;
        this.dateFormatAware = dateFormatAware;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return SickNoteExtendDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        final SickNoteExtendDto extendDto = (SickNoteExtendDto) target;
        final SickNote sickNote = getSickNote(extendDto.getSickNoteId());
        validateEndDate(errors, extendDto, sickNote);
    }

    private void validateEndDate(Errors errors, SickNoteExtendDto dto, SickNote sickNote) {
        final LocalDate sickNoteEndDate = sickNote.getEndDate();
        if (!dto.getEndDate().isAfter(sickNoteEndDate)) {
            final Object[] args = { dateFormatAware.format(sickNoteEndDate) };
            // DTO attribute name
            errors.rejectValue("endDate", ERROR_ENDDATE_FUTURE, args, "");
            // input field of custom date
            // use this to ease handling of css error class
            errors.rejectValue("extendToDate", ERROR_ENDDATE_FUTURE, args, "");
        }
    }

    private SickNote getSickNote(Long sickNoteId) {
        if (sickNoteId == null) {
            throw new IllegalStateException("Cannot validate sickNoteExtension without sickNoteId.");
        }
        return sickNoteService.getById(sickNoteId)
            .orElseThrow(() -> new IllegalStateException("Cannot validate sickNoteExtension with sickNoteId=%s which does not exist.".formatted(sickNoteId)));
    }
}
