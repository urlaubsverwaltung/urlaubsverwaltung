package org.synyx.urlaubsverwaltung.calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.SequenceGenerator;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Inheritance
abstract class CalendarAccessible {

    @Id
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    @GeneratedValue(strategy = SEQUENCE, generator = "calendar_accessible_generator")
    @SequenceGenerator(name = "calendar_accessible_generator", sequenceName = "calendar_accessible_id_seq")
    private Long id;

    private boolean isAccessible = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAccessible() {
        return isAccessible;
    }

    public void setAccessible(boolean accessible) {
        isAccessible = accessible;
    }
}
