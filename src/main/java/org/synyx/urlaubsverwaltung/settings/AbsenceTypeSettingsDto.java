package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import java.util.List;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.BLUE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.CYAN;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.EMERALD;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.GRAY;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.PINK;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.VIOLET;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.YELLOW;

public class AbsenceTypeSettingsDto {

    private List<AbsenceTypeSettingsItemDto> items;

    // hard coded instead of enum.values() to have an explicit list order
    private final List<VacationTypeColor> colors = List.of(GRAY, ORANGE, YELLOW, EMERALD, CYAN, BLUE, VIOLET, PINK);

    public List<AbsenceTypeSettingsItemDto> getItems() {
        return items;
    }

    public void setItems(List<AbsenceTypeSettingsItemDto> items) {
        this.items = items;
    }

    public List<VacationTypeColor> getColors() {
        return colors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsenceTypeSettingsDto that = (AbsenceTypeSettingsDto) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public String toString() {
        return "AbsenceTypeSettingsDto{" +
            "items=" + items +
            '}';
    }
}
