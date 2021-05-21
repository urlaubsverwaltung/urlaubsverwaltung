package org.synyx.urlaubsverwaltung.application.domain;

import java.util.Objects;

public class SpecialLeaveDing {

    private Integer id;
    private SpecialLeave specialLeave;
    private int days;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SpecialLeave getSpecialLeave() {
        return specialLeave;
    }

    public void setSpecialLeave(SpecialLeave specialLeave) {
        this.specialLeave = specialLeave;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public String toString() {
        return "SpecialLeaveDing{" +
            "id=" + id +
            ", specialLeave=" + specialLeave +
            ", days=" + days +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialLeaveDing that = (SpecialLeaveDing) o;
        return specialLeave == that.specialLeave;
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialLeave);
    }
}
