package org.synyx.urlaubsverwaltung.absence.web;

import org.synyx.urlaubsverwaltung.absence.DateRange;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes the bars of one person in the absence overview.
 * <p>
 * A bar is one absence (application or sick note). It spans from the first to the last half day carrying the
 * absence. Non-workdays and public holidays in between belong to the bar as a bridge, anything else ends it.
 * Bars are computed on a timeline wider than the visible range, so a bar continuing beyond the visible range is not
 * drawn as ending there.
 */
final class AbsenceBars {

    enum Half {
        MORNING,
        NOON,
        FULL
    }

    enum Kind {
        SOLID,
        BRIDGE
    }

    enum Status {
        ALLOWED,
        WAITING,
        TEMPORARY_ALLOWED,
        CANCELLATION_REQUESTED
    }

    /**
     * @param key        identity of the absence - half days with the same key belong to the same absence
     * @param color      name of the colour, used as suffix of the css variables {@code --absence-color-*}
     * @param statusText text of the status, {@code null} when the status needs no mention
     */
    record Absence(String key, Status status, String color, String label, String statusText, boolean anonymized) {
    }

    record Gap(String title) {
    }

    /**
     * @param absence the absence on this half day, may be {@code null}
     * @param gap     the non-workday or public holiday on this half day, may be {@code null}
     */
    record HalfDay(Absence absence, Gap gap) {
        static final HalfDay EMPTY = new HalfDay(null, null);
    }

    record Day(HalfDay morning, HalfDay noon) {
        static final Day EMPTY = new Day(HalfDay.EMPTY, HalfDay.EMPTY);
    }

    /**
     * @param gap              the gap of a bridge, {@code null} for a solid piece
     * @param coveredByAbsence whether the half day itself carries the absence - false for a bridge over a gap the
     *                         absence does not cover, e.g. the weekend inside a vacation
     * @param labelHalves      number of half days the label spans, 0 when this piece carries no label
     */
    record Piece(Half half, Kind kind, boolean roundedStart, boolean roundedEnd, Absence absence, Gap gap,
                 boolean coveredByAbsence, int labelHalves) {
    }

    private record SlotPiece(int bar, Absence absence, Kind kind, Gap gap, boolean coveredByAbsence,
                             boolean first, boolean last) {
    }

    private record BarMonth(int bar, YearMonth month) {
    }

    private AbsenceBars() {
        // static helper
    }

    /**
     * @param timeline range the bars are computed on, must contain {@code visible}
     * @param days     half day contents by date, missing dates are empty
     * @param visible  range pieces are returned for; every month in it gets the label of a bar again
     * @return pieces by date, only for visible dates with at least one piece
     */
    static Map<LocalDate, List<Piece>> compute(DateRange timeline, Map<LocalDate, Day> days, DateRange visible) {

        final List<LocalDate> dates = timeline.stream().toList();
        final HalfDay[] slots = new HalfDay[dates.size() * 2];
        for (int d = 0; d < dates.size(); d++) {
            final Day day = days.getOrDefault(dates.get(d), Day.EMPTY);
            slots[2 * d] = day.morning();
            slots[2 * d + 1] = day.noon();
        }

        final SlotPiece[] assigned = assignBars(slots);
        final int[] labelHalves = labelHalves(assigned, dates, visible);
        return pieces(assigned, labelHalves, dates, visible);
    }

    private static SlotPiece[] assignBars(HalfDay[] slots) {

        final SlotPiece[] assigned = new SlotPiece[slots.length];

        int bar = 0;
        int start = 0;
        while (start < slots.length) {
            final Absence absence = slots[start].absence();
            if (absence == null) {
                start++;
                continue;
            }

            int last = start;
            for (int cursor = start + 1; cursor < slots.length; cursor++) {
                final HalfDay slot = slots[cursor];
                if (slot.absence() != null) {
                    if (!slot.absence().key().equals(absence.key())) {
                        break;
                    }
                    last = cursor;
                } else if (slot.gap() == null) {
                    break;
                }
            }

            for (int s = start; s <= last; s++) {
                final HalfDay slot = slots[s];
                final Kind kind = slot.gap() == null ? Kind.SOLID : Kind.BRIDGE;
                assigned[s] = new SlotPiece(bar, absence, kind, slot.gap(), slot.absence() != null, s == start, s == last);
            }

            bar++;
            start = last + 1;
        }

        return assigned;
    }

    private static int[] labelHalves(SlotPiece[] assigned, List<LocalDate> dates, DateRange visible) {

        final int[] labelHalves = new int[assigned.length];
        final Set<BarMonth> labelled = new HashSet<>();

        for (int s = 0; s < assigned.length; s++) {
            final SlotPiece slotPiece = assigned[s];
            final LocalDate date = dates.get(s / 2);
            if (slotPiece == null || slotPiece.kind() != Kind.SOLID || !isVisible(date, visible)) {
                continue;
            }

            final YearMonth month = YearMonth.from(date);
            if (!labelled.add(new BarMonth(slotPiece.bar(), month))) {
                continue;
            }

            int end = s;
            while (end < assigned.length
                && continuesSolidStretch(assigned[end], slotPiece.bar())
                && month.equals(YearMonth.from(dates.get(end / 2)))
                && isVisible(dates.get(end / 2), visible)) {
                end++;
            }
            labelHalves[s] = end - s;
        }

        return labelHalves;
    }

    private static boolean continuesSolidStretch(SlotPiece slotPiece, int bar) {
        return slotPiece != null && slotPiece.bar() == bar && slotPiece.kind() == Kind.SOLID;
    }

    private static Map<LocalDate, List<Piece>> pieces(SlotPiece[] assigned, int[] labelHalves, List<LocalDate> dates, DateRange visible) {

        final Map<LocalDate, List<Piece>> piecesByDate = new HashMap<>();

        for (int d = 0; d < dates.size(); d++) {
            final LocalDate date = dates.get(d);
            if (!isVisible(date, visible)) {
                continue;
            }

            final int morningSlot = 2 * d;
            final int noonSlot = 2 * d + 1;
            final SlotPiece morning = assigned[morningSlot];
            final SlotPiece noon = assigned[noonSlot];

            final List<Piece> pieces = new ArrayList<>(2);
            if (morning != null && noon != null && morning.bar() == noon.bar() && morning.kind() == noon.kind()) {
                final int halves = Math.max(labelHalves[morningSlot], labelHalves[noonSlot]);
                pieces.add(new Piece(Half.FULL, morning.kind(), morning.first(), noon.last(), morning.absence(), morning.gap(), morning.coveredByAbsence(), halves));
            } else {
                if (morning != null) {
                    pieces.add(piece(Half.MORNING, morning, labelHalves[morningSlot]));
                }
                if (noon != null) {
                    pieces.add(piece(Half.NOON, noon, labelHalves[noonSlot]));
                }
            }

            if (!pieces.isEmpty()) {
                piecesByDate.put(date, List.copyOf(pieces));
            }
        }

        return piecesByDate;
    }

    private static Piece piece(Half half, SlotPiece slotPiece, int labelHalves) {
        return new Piece(half, slotPiece.kind(), slotPiece.first(), slotPiece.last(), slotPiece.absence(), slotPiece.gap(), slotPiece.coveredByAbsence(), labelHalves);
    }

    private static boolean isVisible(LocalDate date, DateRange visible) {
        return !date.isBefore(visible.startDate()) && !date.isAfter(visible.endDate());
    }
}
