package org.synyx.urlaubsverwaltung.core.department;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import org.joda.time.DateTime;

import org.springframework.data.jpa.domain.AbstractPersistable;

import org.springframework.util.Assert;

import org.synyx.urlaubsverwaltung.core.person.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


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

    @Temporal(TemporalType.DATE)
    private Date lastModification;

    @CollectionTable(name = "Department_Member")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> members = new ArrayList<>();

    @CollectionTable(name = "Department_DepartmentHead")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> departmentHeads = new ArrayList<>();

    public Department() {

        this.lastModification = DateTime.now().toDate();
    }

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

        return new DateTime(lastModification);
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

        if (members == null) {
            members = Collections.emptyList();
        }

        return Collections.unmodifiableList(members);
    }


    public void setMembers(List<Person> members) {

        this.members = members;
    }


    public List<Person> getDepartmentHeads() {

        if (departmentHeads == null) {
            departmentHeads = Collections.emptyList();
        }

        return Collections.unmodifiableList(departmentHeads);
    }


    public void setDepartmentHeads(List<Person> departmentHeads) {

        this.departmentHeads = departmentHeads;
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
            .append("name", getName())
            .append("members", getMembers().size())
            .append("departmentHeads", getDepartmentHeads().size())
            .toString();
    }
}
