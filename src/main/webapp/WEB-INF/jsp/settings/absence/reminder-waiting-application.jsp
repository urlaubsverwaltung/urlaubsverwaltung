<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.vacation.remindForWaitingApplications.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.vacation.daysBeforeRemindForWaitingApplications.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">

        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="applicationSettings.remindForWaitingApplications.true">
                <spring:message code='settings.vacation.remindForWaitingApplications'/>:
            </label>
            <div class="col-md-8 radio">
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForWaitingApplications.true"
                                      path="applicationSettings.remindForWaitingApplications"
                                      value="true"/>
                    <spring:message code="settings.vacation.remindForWaitingApplications.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="applicationSettings.remindForWaitingApplications.false"
                                      path="applicationSettings.remindForWaitingApplications"
                                      value="false"/>
                    <spring:message
                        code="settings.vacation.remindForWaitingApplications.false"/>
                </label>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="applicationSettings.daysBeforeRemindForWaitingApplications">
                <spring:message
                    code='settings.vacation.daysBeforeRemindForWaitingApplications'/>:
            </label>
            <div class="col-md-8">
                <form:input id="applicationSettings.daysBeforeRemindForWaitingApplications"
                            path="applicationSettings.daysBeforeRemindForWaitingApplications"
                            class="form-control" cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="applicationSettings.daysBeforeRemindForWaitingApplications" />
                </uv:error-text>
            </div>
        </div>

    </div>
</div>
