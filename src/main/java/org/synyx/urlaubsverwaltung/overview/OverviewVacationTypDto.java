package org.synyx.urlaubsverwaltung.overview;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import java.util.Objects;

public class OverviewVacationTypDto {

    private final String label;
    private final VacationCategory category;
    private final VacationTypeColor color;

    OverviewVacationTypDto(String label, VacationCategory category, VacationTypeColor color) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverviewVacationTypDto that = (OverviewVacationTypDto) o;
        return Objects.equals(label, that.label)
            && category == that.category
            && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, category, color);
    }

    @Override
    public String toString() {
        return "OverviewVacationTypDto{" +
            "label='" + label + '\'' +
            ", category=" + category +
            ", color=" + color +
            '}';
    }
}
