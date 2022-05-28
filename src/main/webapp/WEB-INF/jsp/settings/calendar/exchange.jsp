<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.calendar.ews.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.calendar.ews.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.email">
                <spring:message code='settings.calendar.ews.email'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.exchangeCalendarSettings.email"
                            path="calendarSettings.exchangeCalendarSettings.email"
                            class="form-control"
                            cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.exchangeCalendarSettings.email" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.password">
                <spring:message code='settings.calendar.ews.password'/>:
            </label>
            <div class="col-md-8">
                <form:password showPassword="true"
                               id="calendarSettings.exchangeCalendarSettings.password"
                               path="calendarSettings.exchangeCalendarSettings.password"
                               class="form-control"
                               cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.exchangeCalendarSettings.password" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.ewsUrl">
                <spring:message code='settings.calendar.ews.url'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.exchangeCalendarSettings.ewsUrl"
                            path="calendarSettings.exchangeCalendarSettings.ewsUrl"
                            class="form-control"
                            cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.exchangeCalendarSettings.ewsUrl" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.calendar">
                <spring:message code='settings.calendar.ews.calendar'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.exchangeCalendarSettings.calendar"
                            path="calendarSettings.exchangeCalendarSettings.calendar"
                            class="form-control"
                            cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.exchangeCalendarSettings.calendar" />
                </uv:error-text>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.timeZoneId">
                <spring:message code='settings.calendar.ews.timeZoneId'/>:
            </label>
            <div class="col-md-8">
                <form:input
                    id="calendarSettings.exchangeCalendarSettings.timeZoneId"
                    path="calendarSettings.exchangeCalendarSettings.timeZoneId"
                    class="form-control"
                    cssErrorClass="form-control error"
                    list="exchange-timezones"
                />
                <datalist id="exchange-timezones">
                    <c:forEach items="${availableTimezones}" var="timeZoneId">
                        <option value="${timeZoneId}">${timeZoneId}</option>
                    </c:forEach>
                </datalist>
                <uv:error-text>
                    <form:errors path="calendarSettings.provider" />
                </uv:error-text>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.exchangeCalendarSettings.sendInvitationActive">
                <spring:message code='settings.calendar.ews.notification'/>:
            </label>
            <div class="col-md-8 checkbox">
                <label>
                    <form:checkbox
                        id="calendarSettings.exchangeCalendarSettings.sendInvitationActive"
                        path="calendarSettings.exchangeCalendarSettings.sendInvitationActive"
                        value="true"/>
                    <spring:message code="settings.calendar.ews.notification.true"/>
                </label>
            </div>
        </div>
    </div>
</div>
