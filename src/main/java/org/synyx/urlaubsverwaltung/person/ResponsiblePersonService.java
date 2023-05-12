package org.synyx.urlaubsverwaltung.person;

import java.util.List;

public interface ResponsiblePersonService {

    /**
     * Find all persons with one of the roles
     * <ul>
     *     <li>{@linkplain Role#BOSS}</li>
     *     <li>{@linkplain Role#DEPARTMENT_HEAD},</li>
     *     <li>{@linkplain Role#SECOND_STAGE_AUTHORITY}</li>
     * </ul>
     * that are responsible for the given person.
     *
     * @param personOfInterest pivot person
     * @return all persons with one of the mentioned role and responsible for the given person
     */
    List<Person> getResponsibleManagersOf(Person personOfInterest);

    /**
     * Find all persons with role {@linkplain Role#DEPARTMENT_HEAD} that are responsible for the given person.
     *
     * @param personOfInterest pivot person
     * @return all department heads of the given person
     */
    List<Person> getResponsibleDepartmentHeads(Person personOfInterest);

    /**
     * Find all persons with role {@linkplain Role#SECOND_STAGE_AUTHORITY} that are responsible for the given person.
     *
     * @param personOfInterest pivot person
     * @return all second stage authorities of the given person
     */
    List<Person> getResponsibleSecondStageAuthorities(Person personOfInterest);
}
