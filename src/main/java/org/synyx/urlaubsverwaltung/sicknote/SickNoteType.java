package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class SickNoteType extends AbstractPersistable<Integer> {

    @Enumerated(EnumType.STRING)
    private SickNoteCategory category;

    private String messageKey;

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected
        super.setId(id);
    }

    public SickNoteCategory getCategory() {
        return this.category;
    }

    public boolean isOfCategory(SickNoteCategory category) {
        Assert.notNull(category, "Sick note category must be given");
        return getCategory().equals(category);
    }

    public void setCategory(SickNoteCategory category) {
        this.category = category;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    @Override
    public String toString() {
        return "SickNoteType{" +
            "category=" + category +
            ", messageKey='" + messageKey + '\'' +
            '}';
    }
}
