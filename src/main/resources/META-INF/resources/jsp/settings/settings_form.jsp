<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="person" tagdir="/WEB-INF/tags/person" %>

<!DOCTYPE html>
<html>

<head>
    <uv:head />
</head>

<body>

<script type="text/javascript">

    $(document).ready(function () {
        activateTabFromAnchorLink();
    });

    /**
     * when a anchor is defined in the url (#)
     * then it will be opend.
     */
    function activateTabFromAnchorLink() {
        var url = window.location.href;
        var tabName = url.split('#')[1];
        if (tabName) {
            activaTab(tabName);
        }
    }

    function activaTab(tab) {
        $('.nav-tabs a[href="#' + tab + '"]').tab('show');
    }
</script>

<uv:menu />

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <form:form method="POST" action="${URL_PREFIX}/settings" modelAttribute="settings" class="form-horizontal" role="form">
        <form:hidden path="id" />

        <div class="row">
            <div class="col-xs-12 feedback">
                <c:if test="${not empty errors}">
                    <div class="alert alert-danger">
                        <spring:message code="settings.action.update.error" />
                    </div>
                </c:if>
                <c:if test="${success}">
                    <div class="alert alert-success">
                        <spring:message code="settings.action.update.success" />
                    </div>
                </c:if>
            </div>
        </div>

        <c:set var="absenceError">
            <form:errors path="absenceSettings.*"/>
        </c:set>
        <c:if test="${not empty absenceError}">
            <c:set var="ABSENCE_ERROR_CSS_CLASS" value="error"/>
        </c:if>

        <c:set var="workingTimeError">
            <form:errors path="workingTimeSettings.*"/>
        </c:set>
        <c:if test="${not empty workingTimeError}">
            <c:set var="WORKING_TIME_ERROR_CSS_CLASS" value="error"/>
        </c:if>

        <c:set var="mailError">
            <form:errors path="mailSettings.*"/>
        </c:set>

        <c:if test="${not empty mailError}">
            <c:set var="MAIL_ERROR_CSS_CLASS" value="error"/>
        </c:if>

        <c:set var="calendarError">
            <form:errors path="calendarSettings.*"/>
        </c:set>
        <c:if test="${not empty calendarError}">
            <c:set var="CALENDAR_ERROR_CSS_CLASS" value="error"/>
        </c:if>

        <div class="row">
            <div class="col-xs-12">
                <ul class="nav nav-tabs" role="tablist">
                    <li role="presentation" class="active ${ABSENCE_ERROR_CSS_CLASS}">
                        <a href="#absence" aria-controls="absence" role="tab" data-toggle="tab"><spring:message code="settings.tabs.absence" /></a>
                    </li>
                    <li role="presentation" class="${WORKING_TIME_ERROR_CSS_CLASS}">
                        <a href="#publicHolidays" aria-controls="publicHolidays" role="tab" data-toggle="tab"><spring:message code="settings.tabs.workingTime" /></a>
                    </li>
                    <li role="presentation" class="${MAIL_ERROR_CSS_CLASS}">
                        <a href="#mail" aria-controls="mail" role="tab" data-toggle="tab"><spring:message code="settings.tabs.mail" /></a>
                    </li>
                    <li role="presentation" class="${CALENDAR_ERROR_CSS_CLASS}">
                        <a href="#calendar" aria-controls="calendar" role="tab" data-toggle="tab"><spring:message code="settings.tabs.calendar" /></a>
                    </li>
                </ul>
            </div>
        </div>

        <div class="row">
            <div class="tab-content">

            <div class="tab-pane active" id="absence">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.vacation.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.vacation.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.maximumAnnualVacationDays">
                                <spring:message code='settings.vacation.maximumAnnualVacationDays'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="absenceSettings.maximumAnnualVacationDays" path="absenceSettings.maximumAnnualVacationDays" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="absenceSettings.maximumAnnualVacationDays" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.maximumMonthsToApplyForLeaveInAdvance">
                                <spring:message code='settings.vacation.maximumMonthsToApplyForLeaveInAdvance'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="absenceSettings.maximumMonthsToApplyForLeaveInAdvance" path="absenceSettings.maximumMonthsToApplyForLeaveInAdvance" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="absenceSettings.maximumMonthsToApplyForLeaveInAdvance" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.vacation.remindForWaitingApplications.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.vacation.daysBeforeRemindForWaitingApplications.descripton"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.remindForWaitingApplications">
                                <spring:message code='settings.vacation.remindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="absenceSettings.remindForWaitingApplications" path="absenceSettings.remindForWaitingApplications" value="true"/>
                                    <spring:message code="settings.vacation.remindForWaitingApplications.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="absenceSettings.remindForWaitingApplications" path="absenceSettings.remindForWaitingApplications" value="false"/>
                                    <spring:message code="settings.vacation.remindForWaitingApplications.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.daysBeforeRemindForWaitingApplications">
                                <spring:message code='settings.vacation.daysBeforeRemindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="absenceSettings.daysBeforeRemindForWaitingApplications" path="absenceSettings.daysBeforeRemindForWaitingApplications" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="absenceSettings.daysBeforeRemindForWaitingApplications" cssClass="error"/></span>
                            </div>
                        </div>

                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.sickDays.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.sickDays.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.maximumSickPayDays">
                                <spring:message code='settings.sickDays.maximumSickPayDays'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="absenceSettings.maximumSickPayDays" path="absenceSettings.maximumSickPayDays" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="absenceSettings.maximumSickPayDays" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="absenceSettings.daysBeforeEndOfSickPayNotification">
                                <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="absenceSettings.daysBeforeEndOfSickPayNotification" path="absenceSettings.daysBeforeEndOfSickPayNotification" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="absenceSettings.daysBeforeEndOfSickPayNotification" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="tab-pane" id="publicHolidays">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.publicHolidays.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.publicHolidays.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingTimeSettings.workingDurationForChristmasEve">
                                <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="workingTimeSettings.workingDurationForChristmasEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                        <form:option value="${dayLengthType}"><spring:message code="${dayLengthType}" /></form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingTimeSettings.workingDurationForNewYearsEve">
                                <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="workingTimeSettings.workingDurationForNewYearsEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                        <form:option value="${dayLengthType}"><spring:message code="${dayLengthType}" /></form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="federalStateType">
                                <spring:message code='settings.publicHolidays.federalState'/>:
                            </label>

                            <div class="col-md-8">
                                <form:select path="workingTimeSettings.federalState" id="federalStateType" class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${federalStateTypes}" var="federalStateType">
                                        <form:option value="${federalStateType}"><spring:message code="federalState.${federalStateType}" /></form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.overtime.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.overtime.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingTimeSettings.overtimeActive">
                                <spring:message code='settings.overtime.overtimeActive'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="workingTimeSettings.overtimeActive" path="workingTimeSettings.overtimeActive" value="true"/>
                                    <spring:message code="settings.overtime.overtimeActive.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="workingTimeSettings.overtimeActive" path="workingTimeSettings.overtimeActive" value="false"/>
                                    <spring:message code="settings.overtime.overtimeActive.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingTimeSettings.maximumOvertime">
                                <spring:message code="settings.overtime.maximum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="workingTimeSettings.maximumOvertime" path="workingTimeSettings.maximumOvertime" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="workingTimeSettings.maximumOvertime" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workingTimeSettings.minimumOvertime">
                                <spring:message code="settings.overtime.minimum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="workingTimeSettings.minimumOvertime" path="workingTimeSettings.minimumOvertime" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="workingTimeSettings.minimumOvertime" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="tab-pane" id="mail">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.mail.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.mail.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="mailSettings.active">
                                <spring:message code='settings.mail.active'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="mailSettings.active" path="mailSettings.active" value="true"/>
                                    <spring:message code="settings.mail.active.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="mailSettings.active" path="mailSettings.active" value="false"/>
                                    <spring:message code="settings.mail.active.false"/>
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.from">
                                <spring:message code='settings.mail.from'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.from" path="mailSettings.from" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.from" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.administrator">
                                <spring:message code='settings.mail.administrator'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.administrator" path="mailSettings.administrator" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.administrator" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.baseLinkURL">
                                <spring:message code='settings.mail.baseURL'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.baseLinkURL" path="mailSettings.baseLinkURL" placeholder="http://urlaubsverwaltung.mydomain.com/" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.baseLinkURL" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.host">
                                <spring:message code='settings.mail.host'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.host" path="mailSettings.host" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.host" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.port">
                                <spring:message code='settings.mail.port'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.port" path="mailSettings.port" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.port" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.username">
                                <spring:message code='settings.mail.username'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="mailSettings.username" path="mailSettings.username" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.username" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="mailSettings.password">
                                <spring:message code='settings.mail.password'/>:
                            </label>
                            <div class="col-md-8">
                                <form:password showPassword="true" id="mailSettings.password" path="mailSettings.password" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="mailSettings.password" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="tab-pane" id="calendar">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.calendar.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.calendar.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="calendarSettings.workDayBeginHour">
                                <spring:message code='settings.calendar.workDay.begin'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.workDayBeginHour" path="calendarSettings.workDayBeginHour" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.workDayBeginHour" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="calendarSettings.workDayEndHour">
                                <spring:message code='settings.calendar.workDay.end'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.workDayEndHour" path="calendarSettings.workDayEndHour" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.workDayEndHour" cssClass="error"/></span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.calendar.ews.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.calendar.ews.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.active">
                                <spring:message code='settings.calendar.ews.active'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="calendarSettings.exchangeCalendarSettings.active" path="calendarSettings.exchangeCalendarSettings.active" value="true"/>
                                    <spring:message code="settings.calendar.ews.active.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="calendarSettings.exchangeCalendarSettings.active" path="calendarSettings.exchangeCalendarSettings.active" value="false"/>
                                    <spring:message code="settings.calendar.ews.active.false"/>
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.email">
                                <spring:message code='settings.calendar.ews.email'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.exchangeCalendarSettings.email" path="calendarSettings.exchangeCalendarSettings.email" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.exchangeCalendarSettings.email" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.password">
                                <spring:message code='settings.calendar.ews.password'/>:
                            </label>
                            <div class="col-md-8">
                                <form:password showPassword="true" id="calendarSettings.exchangeCalendarSettings.password" path="calendarSettings.exchangeCalendarSettings.password" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.exchangeCalendarSettings.password" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.calendar">
                                <spring:message code='settings.calendar.ews.calendar'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.exchangeCalendarSettings.calendar" path="calendarSettings.exchangeCalendarSettings.calendar" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.exchangeCalendarSettings.calendar" cssClass="error"/></span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.sendInvitationActive">
                                <spring:message code='settings.calendar.ews.notification'/>:
                            </label>
                            <div class="col-md-8 checkbox">
                                <label>
                                    <form:checkbox id="calendarSettings.exchangeCalendarSettings.sendInvitationActive" path="calendarSettings.exchangeCalendarSettings.sendInvitationActive" value="true"/>
                                    <spring:message code="settings.calendar.ews.notification.true"/>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.calendar.google.title" /></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.calendar.google.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="calendarSettings.googleCalendarSettings.active">
                                <spring:message code='settings.calendar.google.active'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="calendarSettings.googleCalendarSettings.active" path="calendarSettings.googleCalendarSettings.active" value="true"/>
                                    <spring:message code="settings.calendar.google.active.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="calendarSettings.googleCalendarSettings.active" path="calendarSettings.googleCalendarSettings.active" value="false"/>
                                    <spring:message code="settings.calendar.google.active.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.googleCalendarSettings.clientId">
                                <spring:message code='settings.calendar.google.clientid'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.googleCalendarSettings.clientId" path="calendarSettings.googleCalendarSettings.clientId" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.googleCalendarSettings.clientId" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.googleCalendarSettings.clientSecret">
                                <spring:message code='settings.calendar.google.clientsecret'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.googleCalendarSettings.clientSecret" path="calendarSettings.googleCalendarSettings.clientSecret" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.googleCalendarSettings.clientSecret" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label class="control-label col-md-4" for="calendarSettings.googleCalendarSettings.calendarId">
                                <spring:message code='settings.calendar.google.calendarid'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="calendarSettings.googleCalendarSettings.calendarId" path="calendarSettings.googleCalendarSettings.calendarId" class="form-control" cssErrorClass="form-control error" />
                                <span class="help-inline"><form:errors path="calendarSettings.googleCalendarSettings.calendarId" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group">
                            <c:choose>
                                <c:when test="${settings.calendarSettings.googleCalendarSettings.refreshToken == null}">
                                    <p class="text-danger col-md-5 col-md-push-4"><spring:message code="settings.calendar.google.action.authenticate.description"/></p>
                                    <button id="googleOAuthButton" value="oauth" name="googleOAuthButton" type="submit" class="btn btn-primary col-md-3 col-md-push-4">
                                        <spring:message code='settings.calendar.google.action.authenticate'/>
                                    </button>
                                </c:when>
                                <c:otherwise>
                                    <p class="text-success col-md-8 col-md-push-4"><spring:message code="settings.calendar.google.action.authenticate.success"/></p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="col-xs-12">
                    <hr/>
                    <p class="help-block"><spring:message code="settings.action.update.description"/></p>
                    <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
                        <spring:message code='action.save'/>
                    </button>
                </div>
            </div>

        </div>
        </div>
        </form:form>
    </div>
</div>

</body>

</html>
