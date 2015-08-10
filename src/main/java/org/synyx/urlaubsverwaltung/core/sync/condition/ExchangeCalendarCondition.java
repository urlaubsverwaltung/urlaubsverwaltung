package org.synyx.urlaubsverwaltung.core.sync.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;

import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * Describes in which case the tool should sync with the Exchange calendar.
 *
 * @author  Aljona Murygina - murygina@synyx.de
 */
public class ExchangeCalendarCondition implements Condition {

    private static final String CALENDAR_PROPERTY_NAME = "calendar";
    private static final String CALENDAR_PROPERTY_VALUE = CalendarType.EWS.getName();

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        String calendarProperty = System.getProperty(CALENDAR_PROPERTY_NAME);

        return calendarProperty != null && calendarProperty.equals(CALENDAR_PROPERTY_VALUE);
    }
}
