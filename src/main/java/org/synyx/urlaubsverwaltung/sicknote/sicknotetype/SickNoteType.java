package org.synyx.urlaubsverwaltung.sicknote.sicknotetype;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteCategory;

import java.util.Objects;

import static jakarta.persistence.EnumType.STRING;

@Entity
public class SickNoteType {

    @Id
    private Long id;

    @NotNull
    @Enumerated(STRING)
    private SickNoteCategory category;

    @NotNull
    private String messageKey;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SickNoteCategory getCategory() {
        return this.category;
    }

    public boolean isOfCategory(SickNoteCategory category) {
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
