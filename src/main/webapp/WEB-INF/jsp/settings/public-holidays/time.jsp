<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.time.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group">
            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.timeZoneId">
                <spring:message code="settings.time.timezone"/>:
            </label>
            <div class="col-md-8">
                <uv:select id="timeSettings.timeZoneId" name="timeSettings.timeZoneId" cssClass="chosenCombo">
                    <c:forEach items="${availableTimezones}" var="timeZoneId">
                        <option value="${timeZoneId}" ${settings.timeSettings.timeZoneId == timeZoneId ? 'selected="selected"' : ''}>
                                ${timeZoneId}
                        </option>
                    </c:forEach>
                </uv:select>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="timeSettings.workDayBeginHour">
                <spring:message code='settings.time.workDay.begin'/>:
            </label>
            <div class="col-md-8">
                <form:input id="timeSettings.workDayBeginHour"
                            path="timeSettings.workDayBeginHour"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="timeSettings.workDayBeginHour" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="timeSettings.workDayEndHour">
                <spring:message code='settings.time.workDay.end'/>:
            </label>
            <div class="col-md-8">
                <form:input id="timeSettings.workDayEndHour"
                            path="timeSettings.workDayEndHour"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="timeSettings.workDayEndHour" />
                </uv:error-text>
            </div>
        </div>
    </div>
</div>
