package org.synyx.urlaubsverwaltung.core.application.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Describes a type of vacation.
 *
 * @author  Johannes Reuter
 * @author  Aljona Murygina
 * @since  2.15.0
 */
@Entity
public class VacationType extends AbstractPersistable<Integer> {

    @Enumerated(EnumType.STRING)
    private VacationCategory category;

    private String displayName;

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
