package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.synyx.urlaubsverwaltung.sicknote.sicknote.extend.SickNoteExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a {@linkplain SickNote} with status {@linkplain SickNoteStatus#SUBMITTED}
 * or a {@linkplain SickNote} with a submitted {@linkplain SickNoteExtension}.
 */
public record SubmittedSickNote(SickNote sickNote, Optional<SickNoteExtension> extension) {

    public SubmittedSickNote(SickNote sickNote) {
        this(sickNote, Optional.empty());
    }

    /**
     * @return start date of the {@linkplain SickNote}
     */
    public LocalDate startDate() {
        return sickNote.getStartDate();
    }

    /**
     * Calculates the next end date of the sickNote after extension is accepted by a privileged user.
     *
     * @return either the end date of the optional {@linkplain SickNoteExtension} or the current {@linkplain SickNote} end date.
     */
    public LocalDate nextEndDate() {
        return extension.map(SickNoteExtension::nextEndDate).orElseGet(sickNote::getEndDate);
    }

    /**
     * Calculates the next workdays the sickNote fills after extension is accepted by a privileged user.
     *
     * @return either next workdays including optional {@linkplain SickNoteExtension} or the current {@linkplain SickNote}.
     */
    public BigDecimal nextWorkdays() {
        final BigDecimal workDays = sickNote.getWorkDays();
        return additionalWorkdays().map(workDays::add).orElse(workDays);
    }

    /**
     * @return empty optional when there is no extension submitted, otherwise the additional workdays.
     */
    public Optional<BigDecimal> additionalWorkdays() {
        return extension.map(SickNoteExtension::additionalWorkdays);
    }

    /**
     * @return {@code true} when an extension has been submitted, {@code false} otherwise
     */
    public boolean extensionSubmitted() {
        return extension.isPresent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubmittedSickNote that = (SubmittedSickNote) o;
        return Objects.equals(sickNote, that.sickNote);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sickNote);
    }
}
