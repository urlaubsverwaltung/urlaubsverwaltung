package org.synyx.urlaubsverwaltung.core.sync.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * Describes in which case the tool should sync with no calendar, i.e. if no or an invalid calendar provider is
 * specified.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class NoCalendarCondition implements Condition {

    private static final String CALENDAR_PROPERTY_NAME = "calendar";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        String calendarProperty = System.getProperty(CALENDAR_PROPERTY_NAME);

        return calendarProperty == null || !CalendarType.contains(calendarProperty);
    }
}
