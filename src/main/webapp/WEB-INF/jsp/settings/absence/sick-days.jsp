<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.sickDays.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.sickDays.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="sickNoteSettings.maximumSickPayDays">
                <spring:message code='settings.sickDays.maximumSickPayDays'/>:
            </label>
            <div class="col-md-8">
                <form:input id="sickNoteSettings.maximumSickPayDays"
                            path="sickNoteSettings.maximumSickPayDays" class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="sickNoteSettings.maximumSickPayDays" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="sickNoteSettings.daysBeforeEndOfSickPayNotification">
                <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
            </label>
            <div class="col-md-8">
                <form:input id="sickNoteSettings.daysBeforeEndOfSickPayNotification"
                            path="sickNoteSettings.daysBeforeEndOfSickPayNotification"
                            class="form-control" cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="sickNoteSettings.daysBeforeEndOfSickPayNotification" />
                </uv:error-text>
            </div>
        </div>
    </div>
</div>
