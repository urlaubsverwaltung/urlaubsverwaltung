<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.vacation.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.vacation.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <c:if test="${defaultVacationDaysFromSettings}">
            <div class="form-group is-required">
                <label class="control-label col-md-4"
                       for="accountSettings.defaultVacationDays">
                    <spring:message code='settings.vacation.defaultVacationDays'/>:
                </label>
                <div class="col-md-8">
                    <form:input id="accountSettings.defaultVacationDays"
                                path="accountSettings.defaultVacationDays"
                                class="form-control"
                                cssErrorClass="form-control error"
                                type="number"
                                step="1"/>
                    <uv:error-text>
                        <form:errors path="accountSettings.defaultVacationDays" />
                    </uv:error-text>
                </div>
            </div>
        </c:if>
        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="accountSettings.maximumAnnualVacationDays">
                <spring:message code='settings.vacation.maximumAnnualVacationDays'/>:
            </label>
            <div class="col-md-8">
                <form:input id="accountSettings.maximumAnnualVacationDays"
                            path="accountSettings.maximumAnnualVacationDays"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number"
                            step="1"/>
                <uv:error-text>
                    <form:errors path="accountSettings.maximumAnnualVacationDays" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="applicationSettings.maximumMonthsToApplyForLeaveInAdvance">
                <spring:message code='settings.vacation.maximumMonthsToApplyForLeaveInAdvance'/>:
            </label>
            <div class="col-md-8">
                <form:input id="applicationSettings.maximumMonthsToApplyForLeaveInAdvance"
                            path="applicationSettings.maximumMonthsToApplyForLeaveInAdvance"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number"
                            step="1"/>
                <uv:error-text>
                    <form:errors path="applicationSettings.maximumMonthsToApplyForLeaveInAdvance" />
                </uv:error-text>
            </div>
        </div>

        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="applicationSettings.allowHalfDays.true">
                <spring:message code='settings.vacation.allowHalfDays'/>:
            </label>
            <div class="col-md-8 radio">
                <label class="halves">
                    <form:radiobutton id="applicationSettings.allowHalfDays.true"
                                      path="applicationSettings.allowHalfDays"
                                      value="true"/>
                    <spring:message code="settings.vacation.allowHalfDays.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="applicationSettings.allowHalfDays.false"
                                      path="applicationSettings.allowHalfDays"
                                      value="false"/>
                    <spring:message
                        code="settings.vacation.allowHalfDays.false"/>
                </label>
            </div>
        </div>

    </div>
</div>
