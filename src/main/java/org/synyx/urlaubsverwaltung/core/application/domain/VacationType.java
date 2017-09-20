package org.synyx.urlaubsverwaltung.core.application.domain;

import lombok.Data;
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
@Data
public class VacationType extends AbstractPersistable<Integer> {

    @Enumerated(EnumType.STRING)
    private VacationCategory category;

    private String displayName;

    @Override
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
    }

    public boolean isOfCategory(VacationCategory category) {

        Assert.notNull(category, "Vacation category must be given");

        return getCategory().equals(category);
    }

}
