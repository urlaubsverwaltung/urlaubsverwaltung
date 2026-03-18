package org.synyx.urlaubsverwaltung.application.statistics;

import java.util.Objects;

public class ApplicationForLeaveStatisticsVacationTypeDto {

    private final String label;
    private final Long id;

    ApplicationForLeaveStatisticsVacationTypeDto(String label, Long id) {
        this.label = label;
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationForLeaveStatisticsVacationTypeDto that = (ApplicationForLeaveStatisticsVacationTypeDto) o;
        return Objects.equals(label, that.label) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, id);
    }
}
