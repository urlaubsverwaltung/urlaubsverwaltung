package org.synyx.urlaubsverwaltung.overlap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.absence.DateRange;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.application.ApplicationService;
import org.synyx.urlaubsverwaltung.period.DayLength;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.ALLOWED_CANCELLATION_REQUESTED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.TEMPORARY_ALLOWED;
import static org.synyx.urlaubsverwaltung.application.application.ApplicationStatus.WAITING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.FULLY_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.NO_OVERLAPPING;
import static org.synyx.urlaubsverwaltung.overlap.OverlapCase.PARTLY_OVERLAPPING;

/**
 * This service handles the validation of {@link Application} for leave concerning overlapping, i.e. if there is already
 * an existent {@link Application} for leave in the same period, the user may not apply for leave in this period.
 */
@Service
public class OverlapService {

    private final ApplicationService applicationService;
    private final SickNoteService sickNoteService;

    @Autowired
    public OverlapService(ApplicationService applicationService, SickNoteService sickNoteService) {
        this.applicationService = applicationService;
        this.sickNoteService = sickNoteService;
    }

    /**
     * Check if there are any overlapping applications for leave or sick notes for the given application for leave.
     *
     * @param application to be checked if there are any overlaps
     * @return {@link OverlapCase} - none, partly, fully
     */
    public OverlapCase checkOverlap(final Application application) {

        final Person person = application.getPerson();
        final LocalDate startDate = application.getStartDate();
        final LocalDate endDate = application.getEndDate();

        List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate, application.getDayLength());
        if (application.getId() != null) {
            applications = applications.stream()
                .filter(input -> input.getId() != null && !input.getId().equals(application.getId()))
                .toList();
        }

        final List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate);

        return getOverlapCase(startDate, endDate, applications, sickNotes);
    }

    /**
     * Check if there are any overlapping applications for leave or sick notes for the given sick note.
     *
     * @param sickNote to be checked if there are any overlaps
     * @return {@link OverlapCase} - none, partly, fully
     */
    public OverlapCase checkOverlap(final SickNote sickNote) {

        final Person person = sickNote.getPerson();
        final LocalDate startDate = sickNote.getStartDate();
        final LocalDate endDate = sickNote.getEndDate();

        final List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate, sickNote.getDayLength());

        List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate);
        if (sickNote.getId() != null) {
            sickNotes = sickNotes.stream()
                .filter(input -> input.getId() != null && !input.getId().equals(sickNote.getId()))
                .toList();
        }

        return getOverlapCase(startDate, endDate, applications, sickNotes);
    }

    /**
     * Get a list of date ranges that overlap with the periods of the given {@link Application}s  and {@link SickNote}s.
     *
     * @param startDate    defines the start of the period
     * @param endDate      defines the end of the period
     * @param applications overlapping the reference application for leave
     * @param sickNotes    overlapping the reference application for leave
     * @return {@link List} of overlap date ranges
     */
    public List<DateRange> getListOfOverlaps(LocalDate startDate, LocalDate endDate, List<Application> applications, List<SickNote> sickNotes) {

        final Stream<DateRange> applicationDateRanges = applications.stream()
            .map(application -> new DateRange(application.getStartDate(), application.getEndDate()));

        final Stream<DateRange> sickNoteDateRanges = sickNotes.stream()
            .map(sickNote -> new DateRange(sickNote.getStartDate(), sickNote.getEndDate()));

        final DateRange periodDateRange = new DateRange(startDate, endDate);
        return Stream.concat(applicationDateRanges, sickNoteDateRanges)
            .map(dateRange -> dateRange.overlap(periodDateRange))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    /**
     * Determine the case of overlap for the given period and overlapping applications for leave and sick notes.
     *
     * @param startDate    defines the start of the period to be checked
     * @param endDate      defines the end of the period to be checked
     * @param applications for leave that are overlapping in the given period
     * @param sickNotes    that are overlapping in the given period
     * @return {@link OverlapCase} - none, partly, fully
     */
    private OverlapCase getOverlapCase(LocalDate startDate, LocalDate endDate, List<Application> applications, List<SickNote> sickNotes) {

        // case (1): no overlap at all
        if (applications.isEmpty() && sickNotes.isEmpty()) {
            return NO_OVERLAPPING;
        }

        // case (2) or (3): overlap

        final List<DateRange> listOfOverlaps = getListOfOverlaps(startDate, endDate, applications, sickNotes);
        final List<DateRange> listOfGaps = getListOfGaps(startDate, endDate, listOfOverlaps);

        // gaps between the intervals mean that you can apply vacation for these periods
        // this is case (3)
        if (!listOfGaps.isEmpty()) {
            /* (3) The period of the new application is part
             * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
             * must be asked if he wants to apply for leave for the not overlapping period of the new
             * application.
             */
            return PARTLY_OVERLAPPING;
        }
        // no gaps mean that period of application is element of other periods of applications
        // i.e. you have no free periods to apply vacation for
        // this is case (2)

        /* (2) The period of
         * the new application is element of an existent application's period; i.e. the new application is not
         * necessary because there is already an existent application for this period.
         */
        return FULLY_OVERLAPPING;
    }

    /**
     * Get all active applications for leave of the given person that are in the given period.
     *
     * @param person    to get overlapping applications for leave for
     * @param startDate defines the start of the period
     * @param endDate   defines the end of the period
     * @param dayLength defines the time of day of the period
     * @return {@link List} of {@link Application}s overlapping with the period
     */
    private List<Application> getRelevantApplicationsForLeave(Person person, LocalDate startDate, LocalDate endDate, DayLength dayLength) {

        // get all applications for leave
        final List<Application> applicationsForLeave = applicationService.getApplicationsForACertainPeriodAndPerson(startDate, endDate, person);

        // remove the non-relevant ones
        return applicationsForLeave.stream()
            .filter(withConflictingStatus().and(withOverlappingDayLength(dayLength)))
            .toList();
    }

    private Predicate<Application> withOverlappingDayLength(DayLength dayLength) {
        return application -> application.getDayLength().isFull() || dayLength.isFull() || application.getDayLength().equals(dayLength);
    }

    private Predicate<Application> withConflictingStatus() {
        return application -> application.hasStatus(WAITING) ||
            application.hasStatus(ALLOWED) ||
            application.hasStatus(ALLOWED_CANCELLATION_REQUESTED) ||
            application.hasStatus(TEMPORARY_ALLOWED);
    }

    /**
     * Get all active sick notes of the given person that are in the given period.
     *
     * @param person    to get overlapping sick notes for
     * @param startDate defines the start of the period
     * @param endDate   defines the end of the period
     * @return {@link List} of {@link SickNote}s overlapping with the period of the given {@link Application}
     */
    private List<SickNote> getRelevantSickNotes(Person person, LocalDate startDate, LocalDate endDate) {
        // only active sick notes are relevant
        return sickNoteService.getByPersonAndPeriod(person, startDate, endDate)
            .stream()
            .filter(SickNote::isActive)
            .toList();
    }

    /**
     * Get a list of gaps within the given intervals.
     *
     * @param startDate      defines the start of the period
     * @param endDate        defines the end of the period
     * @param listOfOverlaps list of overlaps
     * @return {@link List} of gaps
     */
    private List<DateRange> getListOfGaps(LocalDate startDate, LocalDate endDate, List<DateRange> listOfOverlaps) {
        // check start and end points
        if (listOfOverlaps.isEmpty()) {
            return List.of();
        }

        final LocalDate firstOverlapStart = listOfOverlaps.getFirst().startDate();
        final LocalDate lastOverlapEnd = listOfOverlaps.getLast().endDate();

        final List<DateRange> listOfGaps = new ArrayList<>();
        if (startDate.isBefore(firstOverlapStart)) {
            listOfGaps.add(new DateRange(startDate, firstOverlapStart));
        }
        if (endDate.isAfter(lastOverlapEnd)) {
            listOfGaps.add(new DateRange(lastOverlapEnd, endDate));
        }

        // check if intervals abut or gap
        for (int i = 0; (i + 1) < listOfOverlaps.size(); i++) {
            // if they don't abut, you can calculate the gap
            // test if end of interval is equals resp. one day plus of start of other interval
            // e.g. if period 1: 16.-18. and period 2: 19.-20 --> they abut
            // e.g. if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
            if (intervalsHaveGap(listOfOverlaps.get(i), listOfOverlaps.get(i + 1))) {
                final Optional<DateRange> maybeGap = listOfOverlaps.get(i).gap(listOfOverlaps.get(i + 1));
                maybeGap.ifPresent(listOfGaps::add);
            }
        }

        return listOfGaps;
    }

    /**
     * Check if the two given intervals have a gap or if they abut.
     *
     * <p>Some examples:</p>
     * <p>(1) period 16.-18. and period 19.-20. --> they abut</p>
     * <p>(2) period 16.-18. and period 20.-22. --> they have a gap</p>
     *
     * @param firstInterval
     * @param secondInterval
     * @return {@code true} if they have a gap between or {@code false} if they have no gap
     */
    private boolean intervalsHaveGap(DateRange firstInterval, DateRange secondInterval) {

        // test if end of interval is equals resp. one day plus of start of other interval
        final LocalDate endOfFirstInterval = firstInterval.endDate();
        final LocalDate startOfSecondInterval = secondInterval.startDate();

        return !(endOfFirstInterval.equals(startOfSecondInterval) || endOfFirstInterval.plusDays(1).equals(startOfSecondInterval));
    }
}
