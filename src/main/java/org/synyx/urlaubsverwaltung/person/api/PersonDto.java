package org.synyx.urlaubsverwaltung.person.api;

import org.springframework.hateoas.RepresentationModel;
import org.synyx.urlaubsverwaltung.absence.AbsenceApiController;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteApiController;
import org.synyx.urlaubsverwaltung.vacations.VacationApiController;
import org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.synyx.urlaubsverwaltung.absence.AbsenceApiController.ABSENCES;
import static org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNoteApiController.SICKNOTES;
import static org.synyx.urlaubsverwaltung.vacations.VacationApiController.VACATIONS;
import static org.synyx.urlaubsverwaltung.workingtime.WorkDaysCountApiController.WORKDAYS;

public class PersonDto extends RepresentationModel<PersonDto> {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String niceName;
    private boolean active;

    PersonDto(Long id, String email, String firstName, String lastName, String niceName, boolean active) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.niceName = niceName;
        this.active = active;

        this.add(linkTo(methodOn(PersonApiController.class).getPerson(id)).withSelfRel());
        this.add(linkTo(methodOn(AbsenceApiController.class).personsAbsences(id, null, null, List.of("vacation", "sick_note", "public_holiday", "no_workday"))).withRel(ABSENCES));
        this.add(linkTo(methodOn(SickNoteApiController.class).personsSickNotes(id, null, null)).withRel(SICKNOTES));
        this.add(linkTo(methodOn(VacationApiController.class).getVacations(id, null, null, List.of("waiting", "temporary_allowed", "allowed", "allowed_cancellation_requested"))).withRel(VACATIONS));
        this.add(linkTo(methodOn(WorkDaysCountApiController.class).personsWorkDays(id, null, null, null)).withRel(WORKDAYS));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
