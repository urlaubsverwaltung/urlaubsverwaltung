package org.synyx.urlaubsverwaltung.application.vacationtype;

import java.util.Objects;

public class VacationTypeDto {
    private final Integer id;
    private final String color;

    public VacationTypeDto(Integer id, String color) {
        this.id = id;
        this.color = color;
    }

    public Integer getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacationTypeDto that = (VacationTypeDto) o;
        return Objects.equals(id, that.id) && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, color);
    }
}
