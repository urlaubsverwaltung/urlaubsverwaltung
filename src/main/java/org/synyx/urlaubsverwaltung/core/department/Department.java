package org.synyx.urlaubsverwaltung.core.department;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> members = new ArrayList<>();

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


    public DateTime getLastModification() {

        if (lastModification == null) {
            // because DateTime creates DateTime.now() if new DateTime(null) is called
            return null;
        } else {
            return new DateTime(lastModification);
        }
    }


    public void setLastModification(DateTime lastModification) {

        Assert.notNull(lastModification);
        this.lastModification = lastModification.toDate();
    }


    @Override
    public void setId(Integer id) {

        super.setId(id);
    }


    public List<Person> getMembers() {

        return members;
    }


    public void setMembers(List<Person> members) {

        this.members = members;
    }
}
