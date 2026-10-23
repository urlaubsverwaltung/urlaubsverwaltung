package org.synyx.urlaubsverwaltung.blackoutperiod.web;

import java.util.List;

/**
 * An active person the blackout period form shows as affected, depending on the selected departments.
 *
 * @param departmentIds ids of the departments the person is a member of
 */
record BlackoutPeriodPersonDto(String niceName, String initials, String gravatarUrl, List<Long> departmentIds) {
}
