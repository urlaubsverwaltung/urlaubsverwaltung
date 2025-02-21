package org.synyx.urlaubsverwaltung.person.basedata;

import org.synyx.urlaubsverwaltung.person.PersonId;

public record PersonBasedata(
    PersonId personId,
    String personnelNumber,
    String additionalInformation
) {
}
