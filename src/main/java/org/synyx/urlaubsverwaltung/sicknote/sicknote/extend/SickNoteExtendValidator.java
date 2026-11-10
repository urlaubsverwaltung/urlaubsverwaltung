package org.synyx.urlaubsverwaltung.sicknote.sicknote.extend;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.synyx.urlaubsverwaltung.overlap.OverlapCase;
import org.synyx.urlaubsverwaltung.overlap.OverlapService;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;
import org.synyx.urlaubsverwaltung.web.DateFormatAware;

import java.time.Clock;
import java.time.LocalDate;

import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.period.DateLimits.isTooFarInTheFuture;

@Component
class SickNoteExtendValidator implements Validator {

    private static final String ERROR_ENDDATE_FUTURE = "sicknote.extend.validation.constraints.end-date.future.message";
    private static final String ERROR_TOO_FAR_IN_THE_FUTURE = "error.entry.tooFarInTheFuture";
    private static final String ERROR_OVERLAP = "application.error.overlap";

    private final SickNoteService sickNoteService;
    private final OverlapService overlapService;
    private final DateFormatAware dateFormatAware;
    private final Clock clock;

    SickNoteExtendValidator(SickNoteService sickNoteService, OverlapService overlapService, DateFormatAware dateFormatAware, Clock clock) {
        this.sickNoteService = sickNoteService;
        this.overlapService = overlapService;
        this.dateFormatAware = dateFormatAware;
        this.clock = clock;
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
        final LocalDate nextEndDate = dto.getEndDate();
        // a preview can be asked for without a date at all, which is no date after the end of the sick note either
        if (nextEndDate == null || !nextEndDate.isAfter(sickNoteEndDate)) {
            final Object[] args = { dateFormatAware.format(sickNoteEndDate) };
            // DTO attribute name
            errors.rejectValue("endDate", ERROR_ENDDATE_FUTURE, args, "");
            // input field of custom date
            // use this to ease handling of css error class
            errors.rejectValue("extendToDate", ERROR_ENDDATE_FUTURE, args, "");
        } else if (isTooFarInTheFuture(nextEndDate, clock)) {
            errors.rejectValue("endDate", ERROR_TOO_FAR_IN_THE_FUTURE);
            errors.rejectValue("extendToDate", ERROR_TOO_FAR_IN_THE_FUTURE);
        } else if (isOverlapping(SickNote.builder(sickNote).endDate(nextEndDate).build())) {
            // like a new or edited sick note, the extended one must not overlap an application for leave or another sick note
            errors.rejectValue("endDate", ERROR_OVERLAP);
            errors.rejectValue("extendToDate", ERROR_OVERLAP);
        }
    }

    private boolean isOverlapping(SickNote extendedSickNote) {
        final OverlapCase overlap = overlapService.checkOverlap(extendedSickNote);
        return overlap == FULLY_OVERLAPPING || overlap == PARTLY_OVERLAPPING;
    }

    private SickNote getSickNote(Long sickNoteId) {
        if (sickNoteId == null) {
            throw new IllegalStateException("Cannot validate sickNoteExtension without sickNoteId.");
        }
        return sickNoteService.getById(sickNoteId)
            .orElseThrow(() -> new IllegalStateException("Cannot validate sickNoteExtension with sickNoteId=%s which does not exist.".formatted(sickNoteId)));
    }
}
