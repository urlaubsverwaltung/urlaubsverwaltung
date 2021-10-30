package org.synyx.urlaubsverwaltung.application.vacationtype;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;

/**
 * Describes a type of vacation.
 *
 * @since 2.15.0
 */
@Entity(name = "vacation_type")
public class VacationTypeEntity {

    @Id
    private Integer id;

    private boolean active;

    @Enumerated(STRING)
    private VacationCategory category;

    private String messageKey;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean enabled) {
        this.active = enabled;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    public boolean isOfCategory(VacationCategory category) {
        return getCategory().equals(category);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String toString() {
        return "VacationType{" +
            "id=" + id +
            ", active=" + active +
            ", category=" + category +
            ", messageKey='" + messageKey + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VacationTypeEntity that = (VacationTypeEntity) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
