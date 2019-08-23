package org.synyx.urlaubsverwaltung.department;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.util.Assert;
import org.synyx.urlaubsverwaltung.person.Person;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.ZoneOffset.UTC;


/**
 * Department represents an organisation unit of a company.
 */
@Entity
public class Department extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private String name;

    private String description;

    private LocalDate lastModification;

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

        this.lastModification = LocalDate.now(UTC);
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


    public LocalDate getLastModification() {

        return this.lastModification;
    }


    public void setLastModification(LocalDate lastModification) {

        Assert.notNull(lastModification, "Last modification date must be set.");
        this.lastModification = lastModification;
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
        return "Department{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", lastModification=" + lastModification +
            ", twoStageApproval=" + twoStageApproval +
            ", members=" + members +
            ", departmentHeads=" + departmentHeads +
            ", secondStageAuthorities=" + secondStageAuthorities +
            '}';
    }
}
