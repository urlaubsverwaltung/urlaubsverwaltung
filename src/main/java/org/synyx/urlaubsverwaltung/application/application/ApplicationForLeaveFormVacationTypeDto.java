package org.synyx.urlaubsverwaltung.application.application;

import org.synyx.urlaubsverwaltung.application.vacationtype.VacationCategory;

import java.util.Objects;

public class ApplicationForLeaveFormVacationTypeDto {

    private Long id;
    private String label;
    private VacationCategory category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationForLeaveFormVacationTypeDto that = (ApplicationForLeaveFormVacationTypeDto) o;
        return Objects.equals(id, that.id) && Objects.equals(label, that.label) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, category);
    }

    @Override
    public String toString() {
        return "ApplicationForLeaveFormVacationTypeDto{" +
            "id=" + id +
            ", label='" + label + '\'' +
            ", category=" + category +
            '}';
    }
}
