package org.synyx.urlaubsverwaltung.settings;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor;

import java.util.List;
import java.util.Objects;

import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.*;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.FUCHSIA;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.NEUTRAL;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.ORANGE;
import static org.synyx.urlaubsverwaltung.application.vacationtype.VacationTypeColor.RED;

public class AbsenceTypeSettingsDto {

    private List<AbsenceTypeSettingsItemDto> items;

    // hard coded instead of enum.values() to have an explicit list order
    private List<VacationTypeColor> colors = List.of(NEUTRAL, RED, ORANGE, YELLOW, LIME, CYAN, BLUE, VIOLET, FUCHSIA);

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
}
