package org.synyx.urlaubsverwaltung.blackoutperiod.web;

/**
 * A selectable department, rendered as a checkbox in the blackout period form.
 *
 * @param memberCount number of active members of the department
 */
record BlackoutPeriodDepartmentOptionDto(Long id, String label, boolean selected, long memberCount) {
}
