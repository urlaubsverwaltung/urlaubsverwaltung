package org.synyx.urlaubsverwaltung.application.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Enumerated;

import static javax.persistence.EnumType.STRING;


/**
 * Describes a type of vacation.
 *
 * @since 2.15.0
 */
@Entity
public class VacationType extends AbstractPersistable<Integer> {

    @Enumerated(STRING)
    private VacationCategory category;

    private String messageKey;

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected
        super.setId(id);
    }

    public VacationCategory getCategory() {
        return category;
    }

    public void setCategory(VacationCategory category) {
        this.category = category;
    }

    public boolean isOfCategory(VacationCategory category) {
        Assert.notNull(category, "Vacation category must be given");
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
            "category=" + category +
            ", messageKey='" + messageKey + '\'' +
            '}';
    }
}
