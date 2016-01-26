package org.synyx.urlaubsverwaltung.core.sicknote;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


@Entity
public class SickNoteType extends AbstractPersistable<Integer> {

    public static final String SICK_NOTE = "SICK_NOTE";
    public static final String SICK_NOTE_CHILD = "SICK_NOTE_CHILD";

    private String typeName;

    private String typeDisplayName;

    @Override
    public void setId(Integer id) {

        super.setId(id);
    }


    public String getTypeName() {

        return this.typeName;
    }


    public void setTypeName(String typeName) {

        this.typeName = typeName;
    }


    public String getTypeDisplayName() {

        return typeDisplayName;
    }


    public void setTypeDisplayName(String typeDisplayName) {

        this.typeDisplayName = typeDisplayName;
    }


    @Override
    public String toString() {

        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
