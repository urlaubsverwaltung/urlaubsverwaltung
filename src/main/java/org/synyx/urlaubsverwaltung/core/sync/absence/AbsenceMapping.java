package org.synyx.urlaubsverwaltung.core.sync.absence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 * Mapping object between absence (application or sicknote) and sync calendar event.
 *
 * <p>Daniel Hammann - <hammann@synyx.de>.</p>
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceMapping extends AbstractPersistable<Integer> {

    @Column(nullable = false)
    private Integer absenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbsenceType absenceType;

    @Column(nullable = false)
    private String eventId;

}
