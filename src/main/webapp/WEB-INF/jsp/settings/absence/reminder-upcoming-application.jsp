<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.vacation.remindForUpcomingApplications.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.vacation.remindForUpcomingApplications.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="applicationSettings.remindForUpcomingApplications.true">
                <spring:message code='settings.vacation.remindForUpcomingApplications'/>:
            </label>
            <div class="col-md-8 radio">
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForUpcomingApplications.true"
                                      path="applicationSettings.remindForUpcomingApplications"
                                      value="true"/>
                    <spring:message code="settings.vacation.remindForUpcomingApplications.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForUpcomingApplications.false"
                                      path="applicationSettings.remindForUpcomingApplications"
                                      value="false"/>
                    <spring:message
                        code="settings.vacation.remindForUpcomingApplications.false"/>
                </label>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="applicationSettings.daysBeforeRemindForUpcomingApplications">
                <spring:message code='settings.vacation.daysBeforeRemindForUpcomingApplications'/>:
            </label>
            <div class="col-md-8">
                <form:input id="applicationSettings.daysBeforeRemindForUpcomingApplications"
                            path="applicationSettings.daysBeforeRemindForUpcomingApplications"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number"
                            step="1"/>
                <uv:error-text>
                    <form:errors path="applicationSettings.daysBeforeRemindForUpcomingApplications" />
                </uv:error-text>
            </div>
        </div>

    </div>
</div>
