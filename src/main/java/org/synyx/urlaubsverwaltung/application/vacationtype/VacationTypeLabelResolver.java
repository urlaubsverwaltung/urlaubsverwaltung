package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Locale;

@FunctionalInterface
public interface VacationTypeLabelResolver<T extends VacationType<T>> {

    String getLabel(T vacationType, Locale locale);
}
