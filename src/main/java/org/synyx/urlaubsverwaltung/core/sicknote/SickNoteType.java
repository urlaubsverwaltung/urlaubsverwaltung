package org.synyx.urlaubsverwaltung.core.sicknote;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


@Entity
public class SickNoteType extends AbstractPersistable<Integer> {

    @Enumerated(EnumType.STRING)
    private SickNoteCategory category;

    private String displayName;

    @Override
    public void setId(Integer id) { // NOSONAR - override is important here

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


    public String getDisplayName() {

        return displayName;
    }


    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
