<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="applicationSettings.remindForUpcomingHolidayReplacement.activation.true">
                <spring:message code='settings.vacation.remindForUpcomingHolidayReplacement.activation'/>:
            </label>
            <div class="col-md-8 radio">
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForUpcomingHolidayReplacement.activation.true"
                                      path="applicationSettings.remindForUpcomingHolidayReplacement"
                                      value="true"/>
                    <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.activation.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForUpcomingHolidayReplacement.activation.false"
                                      path="applicationSettings.remindForUpcomingHolidayReplacement"
                                      value="false"/>
                    <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.activation.false"/>
                </label>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-4" for="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement">
                <spring:message code='settings.vacation.daysBeforeRemindForUpcomingHolidayReplacement'/>:
            </label>
            <div class="col-md-8">
                <form:input id="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement"
                            path="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement"
                            class="form-control" cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement" />
                </uv:error-text>
            </div>
        </div>
    </div>
</div>
