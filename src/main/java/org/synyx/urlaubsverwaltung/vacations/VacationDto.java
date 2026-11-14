package org.synyx.urlaubsverwaltung.vacations;

import org.springframework.hateoas.RepresentationModel;
import org.synyx.urlaubsverwaltung.application.application.Application;
import org.synyx.urlaubsverwaltung.application.vacationtype.VacationType;
import org.synyx.urlaubsverwaltung.person.api.PersonDto;
import org.synyx.urlaubsverwaltung.person.api.PersonMapper;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.synyx.urlaubsverwaltung.api.RestApiDateFormat.DATE_PATTERN;

public class VacationDto extends RepresentationModel<VacationDto> {

    private static final DateTimeFormatter formatter = ofPattern(DATE_PATTERN);

    private final String from;
    private final String to;
    private final BigDecimal dayLength;
    private final PersonDto person;
    private final String type;
    private final String status;

    VacationDto(Application application) {
        this(application, true);
    }

    /**
     * @param typeVisible {@code false} leaves the type out, e.g. for a colleague's absence of a type not visible to everyone
     */
    VacationDto(Application application, boolean typeVisible) {

        this.from = application.getStartDate().format(formatter);
        this.to = application.getEndDate().format(formatter);
        this.dayLength = application.getDayLength().getDuration();
        this.person = PersonMapper.mapToDto(application.getPerson());
        this.status = application.getStatus().name();

        final VacationType<?> vacationType = application.getVacationType();
        this.type = typeVisible ? vacationType.getCategory().toString() : null;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigDecimal getDayLength() {
        return dayLength;
    }

    public PersonDto getPerson() {
        return person;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }
}
