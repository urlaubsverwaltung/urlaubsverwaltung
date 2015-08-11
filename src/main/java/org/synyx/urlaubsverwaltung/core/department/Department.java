package org.synyx.urlaubsverwaltung.core.department;

import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;


/**
 * Department represents an organisation unit of a company.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */

@Entity
public class Department extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private String name;

    private String description;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastModification;

    public String getName() {

        return name;
    }


    public void setName(String name) {

        this.name = name;
    }


    public String getDescription() {

        return description;
    }


    public void setDescription(String description) {

        this.description = description;
    }


    public Date getLastModification() {

        return lastModification;
    }


    public void setLastModification(DateTime lastModification) {

        this.lastModification = lastModification.toDate();
    }


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }
}
