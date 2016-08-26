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
    <uv:head/>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">
        <form:form method="POST" action="${URL_PREFIX}/settings/calendar" modelAttribute="settings"
                   class="form-horizontal" role="form">
            <div class="row">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.calendar.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.calendar.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workDayBeginHour">
                                <spring:message code='settings.calendar.workDay.begin'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="workDayBeginHour"
                                            path="workDayBeginHour" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="workDayBeginHour" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="workDayEndHour">
                                <spring:message code='settings.calendar.workDay.end'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="workDayEndHour" path="workDayEndHour"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="workDayEndHour" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.calendar.ews.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block">
                            <i class="fa fa-fw fa-info-circle"></i>
                            <spring:message code="settings.calendar.ews.description"/>
                        </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="exchangeCalendarSettings.active">
                                <spring:message code='settings.calendar.ews.active'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="exchangeCalendarSettings.active"
                                                      path="exchangeCalendarSettings.active"
                                                      value="true"/>
                                    <spring:message code="settings.calendar.ews.active.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="exchangeCalendarSettings.active"
                                                      path="exchangeCalendarSettings.active"
                                                      value="false"/>
                                    <spring:message code="settings.calendar.ews.active.false"/>
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4" for="exchangeCalendarSettings.email">
                                <spring:message code='settings.calendar.ews.email'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="exchangeCalendarSettings.email"
                                            path="exchangeCalendarSettings.email" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="exchangeCalendarSettings.email" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4"
                                   for="exchangeCalendarSettings.password">
                                <spring:message code='settings.calendar.ews.password'/>:
                            </label>
                            <div class="col-md-8">
                                <form:password showPassword="true"
                                               id="exchangeCalendarSettings.password"
                                               path="exchangeCalendarSettings.password"
                                               class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="exchangeCalendarSettings.password" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4"
                                   for="exchangeCalendarSettings.calendar">
                                <spring:message code='settings.calendar.ews.calendar'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="exchangeCalendarSettings.calendar"
                                            path="exchangeCalendarSettings.calendar"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="exchangeCalendarSettings.calendar" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4"
                                   for="exchangeCalendarSettings.sendInvitationActive">
                                <spring:message code='settings.calendar.ews.notification'/>:
                            </label>
                            <div class="col-md-8 checkbox">
                                <label>
                                    <form:checkbox id="exchangeCalendarSettings.sendInvitationActive"
                                                   path="exchangeCalendarSettings.sendInvitationActive"
                                                   value="true"/>
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
                        <button type="button" class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right">
                            <spring:message code="action.cancel"/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

</body>

</html>
