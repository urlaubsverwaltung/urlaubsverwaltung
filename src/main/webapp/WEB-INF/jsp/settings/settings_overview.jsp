<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<!DOCTYPE html>
<html lang="${language}">

<spring:url var="URL_PREFIX" value="/web"/>

<head>
    <title>
        <spring:message code="settings.header.title"/>
    </title>
    <uv:custom-head/>
</head>

<body>

<uv:menu/>

<div class="content">
    <div class="container">

        <div class="tw-space-y-12 md:tw-space-y-0 md:tw-grid tw-gap-12 tw-grid-cols-1 md:tw-grid-cols-2">

            <div class="md:tw-col-start-1 md:tw-row-start-1 ">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/account/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="settings.vacation.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-400 tw-text-white">
                            <icon:key className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.defaultVacationDays'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${accountsettings.defaultVacationDays}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.maximumAnnualVacationDays'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${accountsettings.maximumAnnualVacationDays}" />
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-1 md:tw-row-start-2 ">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/application/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            Application settings
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-400 tw-text-white">
                            <icon:key className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.maximumMonthsToApplyForLeaveInAdvance'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.maximumMonthsToApplyForLeaveInAdvance}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.allowHalfDays'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.allowHalfDays}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <h3 class="control-label col-md-8">
                                <spring:message code="settings.vacation.remindForWaitingApplications.title"/>
                            </h3>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.remindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.remindForWaitingApplications}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.daysBeforeRemindForWaitingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.daysBeforeRemindForWaitingApplications}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <h3 class="control-label col-md-8">
                                <spring:message code="settings.vacation.remindForUpcomingApplications.title"/>
                            </h3>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.remindForUpcomingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.remindForUpcomingApplications}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.vacation.daysBeforeRemindForUpcomingApplications'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${applicationsettings.daysBeforeRemindForUpcomingApplications}" />
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-1 md:tw-row-start-3">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/overtime/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="settings.overtime.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-400 tw-text-white">
                            <icon:user-group className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.overtime.overtimeActive'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${overtimesettings.overtimeActive}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.overtime.maximum'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${overtimesettings.maximumOvertime}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.overtime.minimum'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${overtimesettings.minimumOvertime}" />
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>

            <div class="md:tw-col-start-1 md:tw-row-start-4 ">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/specialleave/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            Special leave Settings
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-400 tw-text-white">
                            <icon:key className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>

                    </jsp:body>
                </uv:box>
            </div>

            <div class="md:tw-col-start-2 md:tw-row-start-1">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/sicknote/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="settings.sickDays.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                        <icon:clock className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.sickDays.maximumSickPayDays'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${sicknotesettings.maximumSickPayDays}" />
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${sicknotesettings.daysBeforeEndOfSickPayNotification}"/>
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>

            <div class="md:tw-col-start-2 md:tw-row-start-2">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/time/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="settings.time.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-8 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                            <icon:map className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code="settings.time.timezone"/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${timesettings.timeZoneId}"/>
                            </div>
                        </div>
                        <div class="form-group tw-flex ">
                            <label class="control-label col-md-8" >
                                <spring:message code='settings.time.workDay.begin'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${timesettings.workDayBeginHour}"/>
                            </div>
                        </div>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8" >
                                <spring:message code='settings.time.workDay.end'/>:
                            </label>
                            <div class="col-md-8">
                                <c:out value="${timesettings.workDayEndHour}"/>
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>

            <div class="md:tw-col-start-2 md:tw-row-start-3">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/workingtime/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="settings.workingTime.title"/>
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                            <icon:clock className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                    <div class="form-group tw-flex">
                        <table class="tw-flex tw-text-sm">
                            <caption class="tw-sr-only">
                                <spring:message code="person.details.workingTime.title"/>
                            </caption>
                            <thead class="tw-order-last">
                                <tr>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="MONDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="TUESDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="WEDNESDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="THURSDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="FRIDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="SATURDAY"/>
                                    </th>
                                    <th class="tw-block tw-font-medium" scope="col">
                                        <spring:message code="SUNDAY"/>
                                    </th>
                                </tr>
                            </thead>
                            <tbody class="tw-mr-1 tw-flex">
                                <tr class="tw-flex tw-flex-col">
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.monday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.monday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.tuesday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.tuesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.wednesday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.wednesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.thursday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.thursday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.friday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.friday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.saturday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.saturday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                    <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                        <c:if test="${workingtimesettings.sunday.duration > 0}">
                                            <icon:check-circle />
                                        </c:if>
                                        <span class="tw-sr-only">
                                            <spring:message code="${workingtimesettings.sunday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                        </span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="form-group tw-flex">
                            <label class="control-label col-md-4">
                            <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                        </label>
                        <div class="col-md-8">
                            <spring:message code="${workingtimesettings.workingDurationForChristmasEve}"/>
                        </div>
                    </div>
                    <div class="form-group tw-flex">
                        <label class="control-label col-md-4">
                            <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                        </label>
                        <div class="col-md-8">
                            <spring:message code="${workingtimesettings.workingDurationForNewYearsEve}"/>
                        </div>
                    </div>

                    <div class="form-group tw-flex">
                        <label class="control-label col-md-4" >
                            <spring:message code='settings.publicHolidays.federalState'/>:
                        </label>
                        <div class="col-md-8">
                            <spring:message code="federalState.${workingtimesettings.federalState}"/>
                        </div>
                    </div>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-2 md:tw-row-start-4">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/calendar/settings" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            Calendar settings
                        </h2>
                    </jsp:body>
                </uv:section-heading>
                    <div class="alert alert-danger tw-flex tw-items-center tw-text-sm" role="alert">
                        <icon:speakerphone className="tw-w-4 tw-h-4" solid="true" />
                        &nbsp;Funktion ist deprecated
                    </div>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                        <icon:clock className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <div class="form-group tw-flex">
                            <label class="control-label col-md-8">
                                <spring:message code='settings.calendar.provider'/>:
                            </label>
                            <div class="col-md-8">
                                <spring:message code="settings.calendar.provider.${calendarsettings.provider}"/>
                            </div>
                        </div>
                    </jsp:body>
                </uv:box>
            </div>
        </div>
    </div>
</div>
</body>
</html>
