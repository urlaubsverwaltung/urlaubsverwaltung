<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="settings.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='settings_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<h1 class="tw-sr-only"><spring:message code="settings.header.title" /></h1>

<div class="content">
    <div class="container">
        <form:form method="POST" action="${URL_PREFIX}/settings" modelAttribute="settings" class="form-horizontal"
                   role="form">
            <form:hidden path="id"/>
            <button type="submit" hidden></button>

            <div class="row tw-mb-4">
                <div class="col-xs-12 feedback">
                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code="settings.action.update.error"/>
                        </div>
                    </c:if>
                    <c:if test="${success}">
                        <div class="alert alert-success">
                            <spring:message code="settings.action.update.success"/>
                        </div>
                    </c:if>
                </div>
            </div>

            <c:set var="applicationError">
                <form:errors path="applicationSettings.*"/>
            </c:set>
            <c:set var="sickNoteError">
                <form:errors path="sickNoteSettings.*"/>
            </c:set>
            <c:set var="accountError">
                <form:errors path="accountSettings.*"/>
            </c:set>

            <c:set var="workingTimeError">
                <form:errors path="workingTimeSettings.*"/>
            </c:set>
            <c:set var="timeError">
                <form:errors path="timeSettings.*"/>
            </c:set>
            <c:set var="overtimeError">
                <form:errors path="overtimeSettings.*"/>
            </c:set>

            <c:set var="calendarError">
                <form:errors path="calendarSettings.*"/>
            </c:set>

            <c:set var="hasAbsenceError" value="${not empty applicationError || not empty sickNoteError || not empty accountError}" />
            <c:set var="hasPublicHolidayError" value="${not empty workingTimeError || not empty timeError || not empty overtimeError}" />
            <c:set var="hasCalendarError" value="${not empty calendarError}" />

            <div class="row">
                <div class="col-xs-12">
                    <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="active">
                            <a
                                href="#absence"
                                aria-controls="absence"
                                role="tab"
                                data-toggle="tab"
                                class="${hasAbsenceError ? 'tw-text-red-800' : ''}"
                            >
                                <spring:message code="settings.tabs.absence"/>
                                <c:if test="${hasAbsenceError}">*</c:if>
                            </a>
                        </li>
                        <li role="presentation">
                            <a
                                href="#publicHolidays"
                                aria-controls="publicHolidays"
                                role="tab"
                                data-toggle="tab"
                                class="${hasPublicHolidayError ? 'tw-text-red-800' : ''}"
                                data-test-id="settings-tab-working-time"
                            >
                                <spring:message code="settings.tabs.workingTime"/>
                                <c:if test="${hasPublicHolidayError}">*</c:if>
                            </a>
                        </li>
                        <li role="presentation">
                            <a
                                href="#calendar"
                                aria-controls="calendar"
                                role="tab"
                                data-toggle="tab"
                                class="${hasCalendarError ? 'tw-text-red-800' : ''}"
                            >
                                <spring:message code="settings.tabs.calendar"/>
                                <c:if test="${hasCalendarError}">*</c:if>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="tab-content tw-mb-16">

                <div class="tab-pane active" id="absence">
                    <div class="form-section tw-mb-8">
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
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
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
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
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
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="applicationSettings.maximumMonthsToApplyForLeaveInAdvance" />
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.vacation.remindForWaitingApplications.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.vacation.daysBeforeRemindForWaitingApplications.descripton"/>
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
                    </div>

                    <div class="form-section">
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
                    </div>
                </div>

                <div class="tab-pane" id="publicHolidays">

                    <c:if test="${defaultWorkingTimeFromSettings}">
                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.workingTime.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">

                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.workingTime.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4">
                                        <spring:message code="settings.workingTime.weekdays"/>:
                                    </label>
                                    <c:forEach items="${weekDays}" var="weekDay" varStatus="counter">
                                    <c:if test="${counter.first || counter.count == 5}">
                                    <div class="col-md-4">
                                    </c:if>
                                        <div class="checkbox">
                                            <label for="${weekDay}">
                                                <form:checkbox id="${weekDay}" path="workingTimeSettings.workingDays"
                                                               value="${weekDay.dayOfWeek}"/>
                                                <spring:message code='${weekDay}'/>
                                            </label>
                                        </div>
                                    <c:if test="${counter.last || counter.count == 4}">
                                    </div>
                                    </c:if>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                    </div>
                    </c:if>

                    <div class="form-section tw-mb-8">
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
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.timeZoneId">
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
                                                    path="timeSettings.workDayBeginHour" class="form-control"
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
                                                    path="timeSettings.workDayEndHour" class="form-control"
                                                    cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="timeSettings.workDayEndHour" />
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.publicHolidays.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.publicHolidays.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4"
                                           for="workingTimeSettings.workingDurationForChristmasEve">
                                        <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                                    </label>

                                    <div class="col-md-8">
                                        <uv:select id="dayLengthTypesChristmasEve" name="workingTimeSettings.workingDurationForChristmasEve">
                                            <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                                <option value="${dayLengthType}" ${settings.workingTimeSettings.workingDurationForChristmasEve == dayLengthType ? 'selected="selected"' : ''}>
                                                    <spring:message code="${dayLengthType}"/>
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4"
                                           for="workingTimeSettings.workingDurationForNewYearsEve">
                                        <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                                    </label>

                                    <div class="col-md-8">
                                        <uv:select id="dayLengthTypesNewYearsEve" name="workingTimeSettings.workingDurationForNewYearsEve">
                                            <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                                <option value="${dayLengthType}" ${settings.workingTimeSettings.workingDurationForNewYearsEve == dayLengthType ? 'selected="selected"' : ''}>
                                                    <spring:message code="${dayLengthType}"/>
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="federalStateType">
                                        <spring:message code='settings.publicHolidays.federalState'/>:
                                    </label>

                                    <div class="col-md-8">
                                        <uv:select id="federalStateType" name="workingTimeSettings.federalState">
                                            <c:forEach items="${federalStateTypes}" var="federalStateType">
                                                <option value="${federalStateType}" ${settings.workingTimeSettings.federalState == federalStateType ? 'selected="selected"' : ''}>
                                                    <spring:message code="federalState.${federalStateType}"/>
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.overtime.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.overtime.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">

                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                        <spring:message code='settings.overtime.overtimeActive'/>:
                                    </label>
                                    <div class="col-md-8 radio">
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeActive.true"
                                                              path="overtimeSettings.overtimeActive" value="true"
                                                              data-test-id="setting-overtime-enabled" />
                                            <spring:message code="settings.overtime.overtimeActive.true"/>
                                        </label>
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeActive.false"
                                                              path="overtimeSettings.overtimeActive" value="false"
                                                              data-test-id="setting-overtime-disabled" />
                                            <spring:message code="settings.overtime.overtimeActive.false"/>
                                        </label>
                                    </div>
                                </div>

                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="overtimeSettings.maximumOvertime">
                                        <spring:message code="settings.overtime.maximum"/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="overtimeSettings.maximumOvertime"
                                                    path="overtimeSettings.maximumOvertime" class="form-control"
                                                    cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="overtimeSettings.maximumOvertime" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="overtimeSettings.minimumOvertime">
                                        <spring:message code="settings.overtime.minimum"/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="overtimeSettings.minimumOvertime"
                                                    path="overtimeSettings.minimumOvertime" class="form-control"
                                                    cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="overtimeSettings.minimumOvertime" />
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="tab-pane" id="calendar">

                    <div class="alert alert-danger tw-flex tw-items-center" role="alert">
                        <icon:speakerphone className="tw-w-4 tw-h-4" solid="true" />
                        &nbsp;<spring:message code="settings.calendar.deprecated"/>
                    </div>

                    <div class="form-section">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.calendar.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.calendar.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="calendarSettingsProvider">
                                        <spring:message code='settings.calendar.provider'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <uv:select id="calendarSettingsProvider" name="calendarSettings.provider">
                                            <c:forEach items="${providers}" var="provider">
                                                <option value="${provider}" ${settings.calendarSettings.provider == provider ? 'selected="selected"' : ''}>
                                                    <spring:message code="settings.calendar.provider.${provider}"/>
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section" id="exchange-calendar">
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
                                                    class="form-control" cssErrorClass="form-control error"/>
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
                                                       class="form-control" cssErrorClass="form-control error"/>
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
                                                    class="form-control" cssErrorClass="form-control error"/>
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
                                                    class="form-control" cssErrorClass="form-control error"/>
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
                    </div>

                    <div class="form-section" id="google-calendar">
                        <div class="row">
                            <div class="col-xs-12">
                                <h2><spring:message code="settings.calendar.google.title"/></h2>
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.calendar.google.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.googleCalendarSettings.clientId">
                                        <spring:message code='settings.calendar.google.clientid'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.googleCalendarSettings.clientId"
                                                    path="calendarSettings.googleCalendarSettings.clientId"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="calendarSettings.googleCalendarSettings.clientId" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.googleCalendarSettings.clientSecret">
                                        <spring:message code='settings.calendar.google.clientsecret'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.googleCalendarSettings.clientSecret"
                                                    path="calendarSettings.googleCalendarSettings.clientSecret"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="calendarSettings.googleCalendarSettings.clientSecret" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.googleCalendarSettings.calendarId">
                                        <spring:message code='settings.calendar.google.calendarid'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.googleCalendarSettings.calendarId"
                                                    path="calendarSettings.googleCalendarSettings.calendarId"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="calendarSettings.googleCalendarSettings.calendarId" />
                                        </uv:error-text>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-4">
                                        <spring:message code='settings.calendar.google.redirecturl'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <input class="form-control" type="text"
                                               name="calendarSettings.googleCalendarSettings.authorizedRedirectUrl"
                                               value="${authorizedRedirectUrl}" readonly/>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <c:if test="${not empty oautherrors}">
                                        <p class="text-danger col-md-8 col-md-push-4">
                                                ${oautherrors}
                                        </p>
                                    </c:if>

                                    <c:choose>
                                        <c:when
                                            test="${settings.calendarSettings.googleCalendarSettings.refreshToken == null}">
                                            <p class="text-danger col-md-5 col-md-push-4"><spring:message
                                                code="settings.calendar.google.action.authenticate.description"/></p>
                                            <button id="googleOAuthButton" value="oauth" name="googleOAuthButton"
                                                    type="submit" class="btn btn-primary col-md-3 col-md-push-4">
                                                <spring:message code='settings.calendar.google.action.authenticate'/>
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <p class="text-success col-md-8 col-md-push-4"><spring:message
                                                code="settings.calendar.google.action.authenticate.success"/></p>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <p class="help-block tw-text-sm"><spring:message code="settings.action.update.description"/></p>
                        <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2" data-test-id="settings-save-button">
                            <spring:message code='action.save'/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

</body>

</html>
