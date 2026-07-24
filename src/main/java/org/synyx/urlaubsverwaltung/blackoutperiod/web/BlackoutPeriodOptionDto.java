package org.synyx.urlaubsverwaltung.blackoutperiod.web;

/**
 * A selectable department or vacation type option, rendered as a checkbox in the blackout period form.
 */
record BlackoutPeriodOptionDto(Long id, String label, boolean selected) {
}
