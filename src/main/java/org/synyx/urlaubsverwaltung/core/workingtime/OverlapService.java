package org.synyx.urlaubsverwaltung.core.workingtime;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.synyx.urlaubsverwaltung.core.application.dao.ApplicationDAO;
import org.synyx.urlaubsverwaltung.core.application.domain.Application;
import org.synyx.urlaubsverwaltung.core.application.domain.ApplicationStatus;
import org.synyx.urlaubsverwaltung.core.period.DayLength;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNote;
import org.synyx.urlaubsverwaltung.core.sicknote.SickNoteDAO;

import java.util.ArrayList;
import java.util.List;


/**
 * This service handles the validation of {@link Application} for leave concerning overlapping, i.e. if there is already
 * an existent {@link Application} for leave in the same period, the user may not apply for leave in this period.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Service
public class OverlapService {

    private final ApplicationDAO applicationDAO;
    private final SickNoteDAO sickNoteDAO;

    @Autowired
    public OverlapService(ApplicationDAO applicationDAO, SickNoteDAO sickNoteDAO) {

        this.applicationDAO = applicationDAO;
        this.sickNoteDAO = sickNoteDAO;
    }

    /**
     * Check if there are any overlapping applications for leave or sick notes for the given application for leave.
     *
     * @param  application  to be checked if there are any overlaps
     *
     * @return  {@link OverlapCase} - none, partly, fully
     */
    public OverlapCase checkOverlap(final Application application) {

        Person person = application.getPerson();
        DateMidnight startDate = application.getStartDate();
        DateMidnight endDate = application.getEndDate();

        List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate,
                application.getDayLength());

        if (!application.isNew()) {
            applications = FluentIterable.from(applications).filter(new Predicate<Application>() {

                            @Override
                            public boolean apply(Application input) {

                                // the same application for leave should not be recognized as overlapping
                                return input.getId() != null && !input.getId().equals(application.getId());
                            }
                        }).toList();
        }

        List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate);

        return getOverlapCase(startDate, endDate, applications, sickNotes);
    }


    /**
     * Check if there are any overlapping applications for leave or sick notes for the given sick note.
     *
     * @param  sickNote  to be checked if there are any overlaps
     *
     * @return  {@link OverlapCase} - none, partly, fully
     */
    public OverlapCase checkOverlap(final SickNote sickNote) {

        Person person = sickNote.getPerson();
        DateMidnight startDate = sickNote.getStartDate();
        DateMidnight endDate = sickNote.getEndDate();

        List<Application> applications = getRelevantApplicationsForLeave(person, startDate, endDate,
                sickNote.getDayLength());

        List<SickNote> sickNotes = getRelevantSickNotes(person, startDate, endDate);

        if (!sickNote.isNew()) {
            sickNotes = FluentIterable.from(sickNotes).filter(new Predicate<SickNote>() {

                            @Override
                            public boolean apply(SickNote input) {

                                // the same sick note should not be recognized as overlapping
                                return input.getId() != null && !input.getId().equals(sickNote.getId());
                            }
                        }).toList();
        }

        return getOverlapCase(startDate, endDate, applications, sickNotes);
    }


    /**
     * Determine the case of overlap for the given period and overlapping applications for leave and sick notes.
     *
     * @param  startDate  defines the start of the period to be checked
     * @param  endDate  defines the end of the period to be checked
     * @param  applications  for leave that are overlapping in the given period
     * @param  sickNotes  that are overlapping in the given period
     *
     * @return  {@link OverlapCase} - none, partly, fully
     */
    OverlapCase getOverlapCase(DateMidnight startDate, DateMidnight endDate, List<Application> applications,
        List<SickNote> sickNotes) {

        // case (1): no overlap at all
        if (applications.isEmpty() && sickNotes.isEmpty()) {
            return OverlapCase.NO_OVERLAPPING;
        } else {
            // case (2) or (3): overlap

            List<Interval> listOfOverlaps = getListOfOverlaps(startDate, endDate, applications, sickNotes);
            List<Interval> listOfGaps = getListOfGaps(startDate, endDate, listOfOverlaps);

            // gaps between the intervals mean that you can apply vacation for this periods
            // this is case (3)
            if (!listOfGaps.isEmpty()) {
                /* (3) The period of the new application is part
                 * of an existent application's period, but for a part of it you could apply new vacation; i.e. user
                 * must be asked if he wants to apply for leave for the not overlapping period of the new
                 * application.
                 */
                return OverlapCase.PARTLY_OVERLAPPING;
            }
            // no gaps mean that period of application is element of other periods of applications
            // i.e. you have no free periods to apply vacation for
            // this is case (2)

            /* (2) The period of
             * the new application is element of an existent application's period; i.e. the new application is not
             * necessary because there is already an existent application for this period.
             */
            return OverlapCase.FULLY_OVERLAPPING;
        }
    }


    /**
     * Get all active applications for leave of the given person that are in the given period.
     *
     * @param  person  to get overlapping applications for leave for
     * @param  startDate  defines the start of the period
     * @param  endDate  defines the end of the period
     * @param  dayLength  defines the time of day of the period
     *
     * @return  {@link List} of {@link Application}s overlapping with the period
     */
    private List<Application> getRelevantApplicationsForLeave(Person person, DateMidnight startDate,
        DateMidnight endDate, DayLength dayLength) {

        // get all applications for leave
        List<Application> applicationsForLeave = applicationDAO.getApplicationsForACertainTimeAndPerson(
                startDate.toDate(), endDate.toDate(), person);

        // remove the non-relevant ones
        return FluentIterable.from(applicationsForLeave).filter(new Predicate<Application>() {

                        @Override
                        public boolean apply(Application input) {

                            // only waiting and allowed applications for leave are relevant
                            boolean isWaitingOrAllowed = input.hasStatus(ApplicationStatus.WAITING)
                                || input.hasStatus(ApplicationStatus.ALLOWED);

                            // if only half day, then only the same time of day and full day is relevant
                            if (!DayLength.FULL.equals(dayLength)) {
                                boolean isOverlappingDayLength = input.getDayLength().equals(dayLength)
                                        || input.getDayLength().equals(DayLength.FULL);
                                return isWaitingOrAllowed && isOverlappingDayLength;
                            }

                            return isWaitingOrAllowed;
                        }
                    }).toList();
    }


    /**
     * Get all active sick notes of the given person that are in the given period.
     *
     * @param  person  to get overlapping sick notes for
     * @param  startDate  defines the start of the period
     * @param  endDate  defines the end of the period
     *
     * @return  {@link List} of {@link SickNote}s overlapping with the period of the given {@link Application}
     */
    private List<SickNote> getRelevantSickNotes(Person person, DateMidnight startDate, DateMidnight endDate) {

        // get all sick notes
        List<SickNote> sickNotes = sickNoteDAO.findByPersonAndPeriod(person, startDate.toDate(), endDate.toDate());

        // filter them since only active sick notes are relevant
        return FluentIterable.from(sickNotes).filter(new Predicate<SickNote>() {

                        @Override
                        public boolean apply(SickNote input) {

                            return input.isActive();
                        }
                    }).toList();
    }


    /**
     * Get a list of intervals that overlap with the period of the given {@link Application} for leave.
     *
     * @param  startDate  defines the start of the period
     * @param  endDate  defines the end of the period
     * @param  applicationsForLeave  overlapping the reference application for leave
     * @param  sickNotes  overlapping the reference application for leave
     *
     * @return  {@link List} of overlap intervals
     */
    public List<Interval> getListOfOverlaps(DateMidnight startDate, DateMidnight endDate,
        List<Application> applicationsForLeave, List<SickNote> sickNotes) {

        Interval interval = new Interval(startDate, endDate);

        List<Interval> overlappingIntervals = new ArrayList<>();

        for (Application application : applicationsForLeave) {
            overlappingIntervals.add(new Interval(application.getStartDate(), application.getEndDate()));
        }

        for (SickNote sickNote : sickNotes) {
            overlappingIntervals.add(new Interval(sickNote.getStartDate(), sickNote.getEndDate()));
        }

        List<Interval> listOfOverlaps = new ArrayList<>();

        for (Interval overlappingInterval : overlappingIntervals) {
            Interval overlap = overlappingInterval.overlap(interval);

            // because intervals are inclusive of the start instant, but exclusive of the end instant
            // you have to check if end of interval a is start of interval b

            if (overlappingInterval.getEnd().equals(interval.getStart())) {
                overlap = new Interval(interval.getStart(), interval.getStart());
            }

            if (overlappingInterval.getStart().equals(interval.getEnd())) {
                overlap = new Interval(interval.getEnd(), interval.getEnd());
            }

            // check if they really overlap, else value of overlap would be null
            if (overlap != null) {
                listOfOverlaps.add(overlap);
            }
        }

        return listOfOverlaps;
    }


    /**
     * Get a list of gaps within the given intervals.
     *
     * @param  startDate  defines the start of the period
     * @param  endDate  defines the end of the period
     * @param  listOfOverlaps  list of overlaps
     *
     * @return  {@link List} of gaps
     */
    private List<Interval> getListOfGaps(DateMidnight startDate, DateMidnight endDate, List<Interval> listOfOverlaps) {

        List<Interval> listOfGaps = new ArrayList<>();

        // check start and end points

        if (listOfOverlaps.isEmpty()) {
            return listOfGaps;
        }

        DateMidnight firstOverlapStart = listOfOverlaps.get(0).getStart().toDateMidnight();
        DateMidnight lastOverlapEnd = listOfOverlaps.get(listOfOverlaps.size() - 1).getEnd().toDateMidnight();

        if (startDate.isBefore(firstOverlapStart)) {
            Interval gapStart = new Interval(startDate, firstOverlapStart);
            listOfGaps.add(gapStart);
        }

        if (endDate.isAfter(lastOverlapEnd)) {
            Interval gapEnd = new Interval(lastOverlapEnd, endDate);
            listOfGaps.add(gapEnd);
        }

        // check if intervals abut or gap
        for (int i = 0; (i + 1) < listOfOverlaps.size(); i++) {
            // if they don't abut, you can calculate the gap
            // test if end of interval is equals resp. one day plus of start of other interval
            // e.g. if period 1: 16.-18. and period 2: 19.-20 --> they abut
            // e.g. if period 1: 16.-18. and period 2: 20.-22 --> they have a gap
            if (intervalsHaveGap(listOfOverlaps.get(i), listOfOverlaps.get(i + 1))) {
                Interval gap = listOfOverlaps.get(i).gap(listOfOverlaps.get(i + 1));
                listOfGaps.add(gap);
            }
        }

        return listOfGaps;
    }


    /**
     * Check if the two given intervals have a gap or if they abut.
     *
     * <p>Some examples:</p>
     *
     * <p>(1) period 16.-18. and period 19.-20. --> they abut</p>
     *
     * <p>(2) period 16.-18. and period 20.-22. --> they have a gap</p>
     *
     * @param  firstInterval
     * @param  secondInterval
     *
     * @return  {@code true} if they have a gap between or {@code false} if they have no gap
     */
    private boolean intervalsHaveGap(Interval firstInterval, Interval secondInterval) {

        // test if end of interval is equals resp. one day plus of start of other interval
        DateMidnight endOfFirstInterval = firstInterval.getEnd().toDateMidnight();
        DateMidnight startOfSecondInterval = secondInterval.getStart().toDateMidnight();

        return !(endOfFirstInterval.equals(startOfSecondInterval)
                || endOfFirstInterval.plusDays(1).equals(startOfSecondInterval));
    }
}
