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

<uv:menu />

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <form:form method="PUT" action="${URL_PREFIX}/settings" modelAttribute="settings" class="form-horizontal" role="form">
        <form:hidden path="id" />
    
        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-12 feedback">
                <c:if test="${success}">
                    <div class="alert alert-success">
                        <spring:message code="settings.action.update.success" />
                    </div>
                </c:if>
            </div>
            
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
                        <label class="control-label col-md-4" for="maximumAnnualVacationDays">
                            <spring:message code='settings.vacation.maximumAnnualVacationDays'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="maximumAnnualVacationDays" path="maximumAnnualVacationDays" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="maximumAnnualVacationDays" cssClass="error"/></span>
                        </div>
                    </div>
                    <div class="form-group is-required">
                        <label class="control-label col-md-4" for="maximumMonthsToApplyForLeaveInAdvance">
                            <spring:message code='settings.vacation.maximumMonthsToApplyForLeaveInAdvance'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="maximumMonthsToApplyForLeaveInAdvance" path="maximumMonthsToApplyForLeaveInAdvance" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="maximumMonthsToApplyForLeaveInAdvance" cssClass="error"/></span>
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
                        <label class="control-label col-md-4" for="maximumSickPayDays">
                            <spring:message code='settings.sickDays.maximumSickPayDays'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="maximumSickPayDays" path="maximumSickPayDays" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="maximumSickPayDays" cssClass="error"/></span>
                        </div>
                    </div>
                    <div class="form-group is-required">
                        <label class="control-label col-md-4" for="daysBeforeEndOfSickPayNotification">
                            <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="daysBeforeEndOfSickPayNotification" path="daysBeforeEndOfSickPayNotification" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="daysBeforeEndOfSickPayNotification" cssClass="error"/></span>
                        </div>
                    </div>
                </div>
            </div>
            
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
                        <label class="control-label col-md-4" for="workingDurationForChristmasEve">
                            <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                        </label>

                        <div class="col-md-8">
                            <form:select path="workingDurationForChristmasEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
                                <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                                    <form:option value="${dayLengthType}"><spring:message code="${dayLengthType}" /></form:option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>
                    <div class="form-group is-required">
                        <label class="control-label col-md-4" for="workingDurationForNewYearsEve">
                            <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                        </label>

                        <div class="col-md-8">
                            <form:select path="workingDurationForNewYearsEve" id="dayLengthTypes" class="form-control" cssErrorClass="form-control error">
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
                            <form:select path="federalState" id="federalStateType" class="form-control" cssErrorClass="form-control error">
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
                            <form:input id="mailSettings.password" path="mailSettings.password" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="mailSettings.password" cssClass="error"/></span>
                        </div>
                    </div>
                </div>
            </div>

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
                        <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.domain">
                            <spring:message code='settings.calendar.ews.domain'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="calendarSettings.exchangeCalendarSettings.domain" path="calendarSettings.exchangeCalendarSettings.domain" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="calendarSettings.exchangeCalendarSettings.domain" cssClass="error"/></span>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4" for="calendarSettings.exchangeCalendarSettings.username">
                            <spring:message code='settings.calendar.ews.username'/>:
                        </label>
                        <div class="col-md-8">
                            <form:input id="calendarSettings.exchangeCalendarSettings.username" path="calendarSettings.exchangeCalendarSettings.username" class="form-control" cssErrorClass="form-control error" />
                            <span class="help-inline"><form:errors path="calendarSettings.exchangeCalendarSettings.username" cssClass="error"/></span>
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
                    <hr/>
                    <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
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
