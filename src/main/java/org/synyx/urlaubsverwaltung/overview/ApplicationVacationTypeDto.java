package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

public final class ApplicationVacationTypeDto {

    private final String label;
    private final VacationCategory category;
    private final VacationTypeColor color;

    ApplicationVacationTypeDto(String label, VacationCategory category, VacationTypeColor color) {
        this.label = label;
        this.category = category;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public VacationTypeColor getColor() {
        return color;
    }
}
