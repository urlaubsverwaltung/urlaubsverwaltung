package org.synyx.urlaubsverwaltung.core.application.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;


/**
 * Enum describing which possible types of vacation exist.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 */
@Entity
public class VacationType extends AbstractPersistable<Integer> {

    public static final String HOLIDAY = "HOLIDAY";
    public static final String SPECIALLEAVE = "SPECIALLEAVE";
    public static final String UNPAIDLEAVE = "UNPAIDLEAVE";
    public static final String OVERTIME = "OVERTIME";

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
