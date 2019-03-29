package org.synyx.urlaubsverwaltung.department;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Department represents an organisation unit of a company.
 */
@Entity
public class Department extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private String name;

    private String description;

    @Temporal(TemporalType.DATE)
    private Date lastModification;

    // flag for two stage approval process
    private boolean twoStageApproval;

    @CollectionTable(name = "Department_Member")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> members = new ArrayList<>();

    @CollectionTable(name = "Department_DepartmentHead")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> departmentHeads = new ArrayList<>();

    @CollectionTable(name = "Department_SecondStageAuthority")
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Person> secondStageAuthorities = new ArrayList<>();

    public Department() {

        this.lastModification = DateTime.now().withTimeAtStartOfDay().toDate();
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
    public void setId(Integer id) { // NOSONAR - make it public instead of protected

        super.setId(id);
    }


    public boolean isTwoStageApproval() {

        return twoStageApproval;
    }


    public void setTwoStageApproval(boolean twoStageApproval) {

        this.twoStageApproval = twoStageApproval;
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


    public List<Person> getSecondStageAuthorities() {

        if (secondStageAuthorities == null) {
            secondStageAuthorities = Collections.emptyList();
        }

        return Collections.unmodifiableList(secondStageAuthorities);
    }


    public void setSecondStageAuthorities(List<Person> secondStageAuthorities) {

        this.secondStageAuthorities = secondStageAuthorities;
    }


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
            .append("name", getName())
            .append("members", getMembers().size())
            .append("departmentHeads", getDepartmentHeads().size())
            .append("secondStageAuthorities", getSecondStageAuthorities().size())
            .toString();
    }
}
