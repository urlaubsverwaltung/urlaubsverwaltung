package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Objects;

import static javax.persistence.EnumType.STRING;

@Entity
public class SickNoteType {

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(STRING)
    private SickNoteCategory category;

    private String messageKey;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SickNoteType that = (SickNoteType) o;
        return null != this.getId() && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
