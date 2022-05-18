package org.synyx.urlaubsverwaltung.overview;

public class VacationTypeDto {
    private final Integer id;
    private final String color;

    VacationTypeDto(Integer id, String color) {
        this.id = id;
        this.color = color;
    }

    public Integer getId() {
        return id;
    }

    public String getColor() {
        return color;
    }
}
