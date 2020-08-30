package org.synyx.urlaubsverwaltung.overlap;

/**
 * <p>This enum describes what kind of overlap occurs if two (or more) applications are overlapping. There are
 * three possible cases: (1) The period of the new application has no overlap at all with existent
 * applications; i.e. you can calculate the normal way and save the application if there are enough vacation
 * days on person's holidays account. (2) The period of the new application is element of an existent
 * application's period; i.e. the new application is not necessary because there is already an existent
 * application for this period. (3) The period of the new application is part of an existent application's
 * period, but for a part of it you could apply new vacation; i.e. user must be asked if he wants to apply for
 * leave for the not overlapping period of the new application.</p>
 */
public enum OverlapCase {

    NO_OVERLAPPING,
    FULLY_OVERLAPPING,
    PARTLY_OVERLAPPING
}
