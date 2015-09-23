package org.synyx.urlaubsverwaltung.core.department;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;


/**
 * Department represents an organisation unit of a company.
 *
 * @author  Daniel Hammann - <hammann@synyx.de>
 */

@Entity
@Data
public class Department extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private String name;

    private String description;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date lastModification;

    @CollectionTable(name = "Department_Member")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> members = new ArrayList<>();

    @CollectionTable(name = "Department_DepartmentHead")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> departmentHeads = new ArrayList<>();


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

}
