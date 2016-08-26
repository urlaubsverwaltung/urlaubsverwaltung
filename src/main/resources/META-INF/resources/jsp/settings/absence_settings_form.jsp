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
        <form:form method="POST" action="${URL_PREFIX}/settings/absence" modelAttribute="settings"
                   class="form-horizontal" role="form">
            <div class="row">
                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.vacation.title"/></legend>
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
                                <form:input id="maximumAnnualVacationDays"
                                            path="maximumAnnualVacationDays" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="maximumAnnualVacationDays" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="maximumMonthsToApplyForLeaveInAdvance">
                                <spring:message code='settings.vacation.maximumMonthsToApplyForLeaveInAdvance'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="maximumMonthsToApplyForLeaveInAdvance"
                                            path="maximumMonthsToApplyForLeaveInAdvance" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="maximumMonthsToApplyForLeaveInAdvance" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.vacation.remindForWaitingApplications.title"/></legend>
                    </div>
                    <div class="col-md-4 col-md-push-8">
                    <span class="help-block">
                        <i class="fa fa-fw fa-info-circle"></i>
                        <spring:message code="settings.vacation.daysBeforeRemindForWaitingApplications.descripton"/>
                    </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="remindForWaitingApplications">
                                <spring:message code='settings.vacation.remindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="remindForWaitingApplications"
                                                      path="remindForWaitingApplications" value="true"/>
                                    <spring:message code="settings.vacation.remindForWaitingApplications.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="remindForWaitingApplications"
                                                      path="remindForWaitingApplications" value="false"/>
                                    <spring:message code="settings.vacation.remindForWaitingApplications.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="daysBeforeRemindForWaitingApplications">
                                <spring:message code='settings.vacation.daysBeforeRemindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="daysBeforeRemindForWaitingApplications"
                                            path="daysBeforeRemindForWaitingApplications" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="daysBeforeRemindForWaitingApplications" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-section">
                    <div class="col-xs-12">
                        <legend><spring:message code="settings.sickDays.title"/></legend>
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
                                <form:input id="maximumSickPayDays" path="maximumSickPayDays"
                                            class="form-control" cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="maximumSickPayDays" cssClass="error"/>
                                </span>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="daysBeforeEndOfSickPayNotification">
                                <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="daysBeforeEndOfSickPayNotification"
                                            path="daysBeforeEndOfSickPayNotification" class="form-control"
                                            cssErrorClass="form-control error"/>
                                <span class="help-inline">
                                    <form:errors path="daysBeforeEndOfSickPayNotification" cssClass="error"/>
                                </span>
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
