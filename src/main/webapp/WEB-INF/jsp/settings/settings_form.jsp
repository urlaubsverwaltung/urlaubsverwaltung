<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">

<head>
    <title>
        <spring:message code="settings.header.title"/>
    </title>
    <uv:custom-head/>
    <script src="https://unpkg.com/@popperjs/core@2.11.5/dist/umd/popper.min.js"></script>
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
                                href="#absenceTypes"
                                aria-controls="absenceTypes"
                                role="tab"
                                data-toggle="tab"
                            >
                                <spring:message code="settings.tabs.absenceTypes"/>
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
                                    <spring:message code="settings.vacation.daysBeforeRemindForWaitingApplications.description"/>
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

                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="applicationSettings.remindForUpcomingHolidayReplacement.activation.true">
                                        <spring:message code='settings.vacation.remindForUpcomingHolidayReplacement.activation'/>:
                                    </label>
                                    <div class="col-md-8 radio">
                                        <label class="halves">
                                            <form:radiobutton id="applicationSettings.remindForUpcomingHolidayReplacement.activation.true"
                                                              path="applicationSettings.remindForUpcomingHolidayReplacement"
                                                              value="true"/>
                                            <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.activation.true"/>
                                        </label>
                                        <label class="halves">
                                            <form:radiobutton id="applicationSettings.remindForUpcomingHolidayReplacement.activation.false"
                                                              path="applicationSettings.remindForUpcomingHolidayReplacement"
                                                              value="false"/>
                                            <spring:message code="settings.vacation.remindForUpcomingHolidayReplacement.activation.false"/>
                                        </label>
                                    </div>
                                </div>

                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement">
                                        <spring:message code='settings.vacation.daysBeforeRemindForUpcomingHolidayReplacement'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement"
                                                    path="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement"
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="applicationSettings.daysBeforeRemindForUpcomingHolidayReplacement" />
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.vacation.remindForUpcomingApplications.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.vacation.remindForUpcomingApplications.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="applicationSettings.remindForUpcomingApplications.true">
                                        <spring:message code='settings.vacation.remindForUpcomingApplications'/>:
                                    </label>
                                    <div class="col-md-8 radio">
                                        <label class="halves">
                                            <form:radiobutton id="applicationSettings.remindForUpcomingApplications.true"
                                                              path="applicationSettings.remindForUpcomingApplications"
                                                              value="true"/>
                                            <spring:message code="settings.vacation.remindForUpcomingApplications.true"/>
                                        </label>
                                        <label class="halves">
                                            <form:radiobutton id="applicationSettings.remindForUpcomingApplications.false"
                                                              path="applicationSettings.remindForUpcomingApplications"
                                                              value="false"/>
                                            <spring:message
                                                code="settings.vacation.remindForUpcomingApplications.false"/>
                                        </label>
                                    </div>
                                </div>

                                <div class="form-group is-required">
                                    <label class="control-label col-md-4"
                                           for="applicationSettings.daysBeforeRemindForUpcomingApplications">
                                        <spring:message code='settings.vacation.daysBeforeRemindForUpcomingApplications'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="applicationSettings.daysBeforeRemindForUpcomingApplications"
                                                    path="applicationSettings.daysBeforeRemindForUpcomingApplications"
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="applicationSettings.daysBeforeRemindForUpcomingApplications" />
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

                <div class="tab-pane" id="absenceTypes">
                    <div class="form-section tw-mb-12">
                        <uv:section-heading>
                            <h2>
                                <spring:message code='settings.absenceTypes.title'/>
                            </h2>
                        </uv:section-heading>
                        <div class="tw-flex tw-flex-col lg:tw-flex-row lg:tw-flex-row-reverse">
                            <div class="help-block tw-flex tw-flex-auto tw-justify-left tw-items-start lg:tw-flex-none lg:tw-w-1/3 lg:tw-ml-3 lg:tw-pl-5 tw-pt-2 lg:tw-pt-0 tw-text-sm">
                                <div>
                                    <icon:information-circle className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                </div>
                                <div class="tw-flex tw-flex-col">
                                    <p>
                                        <spring:message code="settings.absenceTypes.help.1"/>
                                        <a class="tw-inline-flex tw-items-center" target="_blank" rel="noopener" href="https://urlaubsverwaltung.cloud/hilfe/abwesenheiten/#welche-abwesenheitsarten-gibt-es">
                                            <spring:message code="settings.absenceTypes.help.2"/>
                                            <icon:external-link className="tw-ml-1 tw-h-4 tw-w-4" />
                                        </a>
                                    </p>
                                    <p>
                                        <spring:message code="settings.absenceTypes.description.1"/>
                                        <a class="tw-flex tw-items-center" href="mailto:info@urlaubsverwaltung.cloud?subject=Weitere%20Abwesenheitsart">
                                            <icon:mail className="tw-mr-1 tw-h-4 tw-w-4" />
                                            <spring:message code="settings.absenceTypes.description.2"/>
                                        </a>
                                    </p>
                                </div>
                            </div>
                            <table id="absence-type-table" class="lg:tw-flex-1 absence-type-settings-table">
                                <thead>
                                    <tr>
                                        <th scope="col">
                                            <spring:message code='settings.absenceTypes.table.head.state' />
                                        </th>
                                        <th scope="col">
                                            <spring:message code='settings.absenceTypes.table.head.type' />
                                        </th>
                                        <th scope="col">
                                            <spring:message code='settings.absenceTypes.table.head.category' />
                                        </th>
                                        <th scope="col" class="sm:tw-text-right sm:tw-relative">
                                            <span class="th-cell-overflow-text sm:tw-absolute sm:tw-top-1 sm:tw-right-4 tw-whitespace-nowrap">
                                                <spring:message code='settings.absenceTypes.table.head.approval' />
                                            </span>
                                        </th>
                                        <th scope="col">
                                            <spring:message code='settings.absenceTypes.table.head.color' />
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${settings.absenceTypeSettings.items}" var="absenceType" varStatus="loop">
                                    <tr data-enabled="${absenceType.active}">
                                        <td data-col-status data-th-text="<spring:message code='settings.absenceTypes.table.head.state' />">
                                            <span class="checkbox-switch">
                                                <form:hidden path="absenceTypeSettings.items[${loop.index}].id" value="${absenceType.id}" />
                                                <form:checkbox path="absenceTypeSettings.items[${loop.index}].active" id="absenceType-active-${loop.index}" />
                                                <label for="absenceType-active-${loop.index}" class="tw-sr-only">
                                                    <spring:message code="settings.absenceTypes.action.state.label" />
                                                </label>
                                            </span>
                                        </td>
                                        <td data-th-text="<spring:message code='settings.absenceTypes.table.head.type' />">
                                            <spring:message code="${absenceType.messageKey}" />
                                        </td>
                                        <td data-th-text="<spring:message code='settings.absenceTypes.table.head.category' />">
                                            <spring:message code="${absenceType.category}" />
                                        </td>
                                        <td
                                            data-th-text="<spring:message code='settings.absenceTypes.table.head.approval' />"
                                            class="sm:tw-text-right"
                                        >
                                            <form:checkbox
                                                path="absenceTypeSettings.items[${loop.index}].requiresApproval"
                                                id="absenceType-approval-${loop.index}"
                                                cssClass="absence-type-approval-checkbox"
                                            />
                                            <label for="absenceType-approval-${loop.index}" class="tw-sr-only">
                                                <spring:message code="settings.absenceTypes.action.approve.label" />
                                            </label>
                                        </td>
                                        <td data-th-text="<spring:message code='settings.absenceTypes.table.head.color' />">
                                            <label for="color-${loop.index}" class="tw-sr-only">
                                                <spring:message code="settings.absenceTypes.action.color.label" />
                                            </label>

                                            <%-- //::todo set checked radio--%>
                                            <div class="tw-mt-2 tw-p-2 tw-rounded-md tw-border-gray-400 tw-border-2  tw-grid-flow-row-dense tw-grid-cols-3 tw-grid-rows-3 tw-gap-1" id="color-chooser-${loop.index}-popup" style="background-color:black; display: none">
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#D4D4D4'}">checked</c:if> value="#D4D4D4" id="color-chooser-${loop.index}-radio-1" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#D4D4D4;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#FCA5A5'}">checked</c:if> value="#FCA5A5" id="color-chooser-${loop.index}-radio-2" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#FCA5A5;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#FDBA74'}">checked</c:if> value="#FDBA74" id="color-chooser-${loop.index}-radio-3" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#FDBA74;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#FDE047'}">checked</c:if> value="#FDE047" id="color-chooser-${loop.index}-radio-4" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#FDE047;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#BEF264'}">checked</c:if> value="#BEF264" id="color-chooser-${loop.index}-radio-5" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#BEF264;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#67E8F9'}">checked</c:if> value="#67E8F9" id="color-chooser-${loop.index}-radio-6" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#67E8F9;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#93C5FD'}">checked</c:if> value="#93C5FD" id="color-chooser-${loop.index}-radio-7" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#93C5FD;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#C4B5FD'}">checked</c:if> value="#C4B5FD" id="color-chooser-${loop.index}-radio-8" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#C4B5FD;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                                <label style="display:flex;"><input <c:if test="${absenceType.color == '#F0ABFC'}">checked</c:if> value="#F0ABFC" id="color-chooser-${loop.index}-radio-9" name="absenceTypeSettings.items[${loop.index}].color" class="tw-hidden" type="radio" ><div style="background-color:#F0ABFC;" class="tw-w-8 tw-h-8 tw-rounded-md tw-cursor-pointer"></div></label>
                                            </div>
                                            <button type="button" class="tw-px-3 tw-py-2 tw-rounded" id="color-chooser-${loop.index}" style="background-color:${absenceType.color}">
                                                <svg class="tw-w-6 tw-h-6" fill="none" stroke="black" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 21a4 4 0 01-4-4V5a2 2 0 012-2h4a2 2 0 012 2v12a4 4 0 01-4 4zm0 0h12a2 2 0 002-2v-4a2 2 0 00-2-2h-2.343M11 7.343l1.657-1.657a2 2 0 012.828 0l2.829 2.829a2 2 0 010 2.828l-8.486 8.485M7 17h.01"></path>
                                                </svg>
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <script defer type="text/javascript">
                                    var hidden = true;
                                    
                                    const colorChoosers = document.querySelectorAll('button[id^="color-chooser-"]')
                                    for (const colorChooser of colorChoosers) {
                                        colorChooser.addEventListener("click", function () {
                                            hidden = !hidden;
                                            popup.style.display = hidden ? "none" : "grid";
                                        });

                                        const radios = document.querySelectorAll("input[id^=" + colorChooser.id +"-radio]");
                                        for (const r of radios)
                                            r.addEventListener("click", function () {
                                                hidden = !hidden;
                                                popup.style.display = "none";
                                                colorChooser.style.backgroundColor = r.value;
                                            });

                                        const popup = document.querySelector("div[id^=" + colorChooser.id +"-popup]");
                                        Popper.createPopper(colorChooser, popup, {
                                            placement: 'bottom',
                                        });

                                    }
                                </script>
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="form-section">
                        <uv:section-heading>
                            <h2>
                                <spring:message code='settings.specialleave.title'/>
                            </h2>
                        </uv:section-heading>
                        <div class="tw-flex tw-flex-col lg:tw-flex-row lg:tw-flex-row-reverse">
                            <div class="help-block tw-flex tw-flex-auto tw-justify-left tw-items-start lg:tw-flex-none lg:tw-w-1/3 lg:tw-ml-3 lg:tw-pl-5 tw-pt-2 lg:tw-pt-0 tw-text-sm">
                                <div>
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                </div>
                                <div class="tw-flex tw-flex-col">
                                    <p>
                                        <spring:message code="settings.specialleave.help"/>
                                    </p>
                                    <p>
                                        <spring:message code="settings.specialleave.description.1"/>
                                        <a class="tw-flex tw-items-center" href="mailto:info@urlaubsverwaltung.cloud?subject=Missing%20special%20leaves">
                                            <icon:mail className="tw-mr-1 tw-h-4 tw-w-4" />
                                            <spring:message code="settings.specialleave.description.2"/>
                                        </a>
                                    </p>
                                </div>
                            </div>
                            <table id="special-leave-table" class="lg:tw-flex-1 absence-type-settings-table">
                                <caption class="tw-sr-only">
                                    <spring:message code='settings.specialleave.title'/>
                                </caption>
                                <thead>
                                    <tr>
                                        <th scope="col">
                                            <spring:message code='settings.specialleave.table.head.state' />
                                        </th>
                                        <th scope="col">
                                            <spring:message code='settings.specialleave.table.head.type' />
                                        </th>
                                        <th scope="col">
                                            <spring:message code='settings.specialleave.table.head.days' />
                                        </th>
                                    </tr>
                                </thead>
                                <tbody>
                                <c:forEach items="${settings.specialLeaveSettings.specialLeaveSettingsItems}" var="specialLeaveSettingsItem" varStatus="loop">
                                    <tr data-enabled="${specialLeaveSettingsItem.active}">
                                        <td data-col-status data-th-text="<spring:message code='settings.specialleave.table.head.state' />">
                                            <span class="checkbox-switch">
                                                <form:hidden path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].id" value="${specialLeaveSettingsItem.id}" />
                                                <form:checkbox path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].active" id="specialLeave-active-${loop.index}" />
                                                <label for="specialLeave-active-${loop.index}" class="tw-sr-only">
                                                    <spring:message code="settings.specialleave.action.state.label" />
                                                </label>
                                            </span>
                                        </td>
                                        <td data-th-text="<spring:message code='settings.specialleave.table.head.type' />">
                                            <span class="tw-w-1/2 md:tw-w-full">
                                                <spring:message code="${specialLeaveSettingsItem.messageKey}" />
                                            </span>
                                        </td>
                                        <td data-th-text="<spring:message code='settings.specialleave.table.head.days' />">
                                            <form:input id="specialLeave-days-${loop.index}" cssClass="form-control tw-w-1/2 sm:tw-w-full"
                                                        path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].days" class="form-control"
                                                        cssErrorClass="form-control error"
                                                        type="number" step="1" min="0"/>
                                            <uv:error-text>
                                                <form:errors path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].days" />
                                            </uv:error-text>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
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
                        <c:set var="workingTimeError">
                            <form:errors path="workingTimeSettings.workingDays" />
                        </c:set>
                        <c:if test="${not empty workingTimeError}">
                            <div class="row tw-mb-8">
                                <div class="col-xs-12">
                                    <div class="alert alert-danger tw-text-red-800">${workingTimeError}</div>
                                </div>
                            </div>
                        </c:if>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.workingTime.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4 tw-mb-4">
                                        <spring:message code="settings.workingTime.weekdays"/>:
                                    </label>
                                    <div class="col-md-8">
                                        <c:forEach items="${weekDays}" var="weekDay">
                                            <div class="checkbox">
                                                <label for="${weekDay}">
                                                    <form:checkbox id="${weekDay}" path="workingTimeSettings.workingDays"
                                                                   value="${weekDay.value}"/>
                                                    <spring:message code='${weekDay}'/>
                                                </label>
                                            </div>
                                        </c:forEach>
                                    </div>
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
                                            <c:set var="countryLabelGeneral"><spring:message code="country.general" /></c:set>
                                            <optgroup label="${countryLabelGeneral}">
                                                <option value="NONE" ${settings.workingTimeSettings.federalState == "NONE" ? 'selected="selected"' : ''}>
                                                    <spring:message code="federalState.NONE"/>
                                                </option>
                                            </optgroup>
                                            <c:forEach items="${federalStateTypes}" var="federalStatesByCountry">
                                                <c:set var="countryLabel"><spring:message code="country.${federalStatesByCountry.key}" /></c:set>
                                                <optgroup label="${countryLabel}">
                                                    <c:forEach items="${federalStatesByCountry.value}" var="federalStateType">
                                                        <option value="${federalStateType}" ${settings.workingTimeSettings.federalState == federalStateType ? 'selected="selected"' : ''}>
                                                            <spring:message code="federalState.${federalStateType}"/>
                                                        </option>
                                                    </c:forEach>
                                                </optgroup>
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
                                    <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                        <spring:message code='settings.overtime.overtimeWritePrivilegedOnly'/>:
                                    </label>
                                    <div class="col-md-8 radio">
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeWritePrivilegedOnly.true"
                                                              path="overtimeSettings.overtimeWritePrivilegedOnly" value="true"
                                                              data-test-id="setting-overtime-write-privileged-only-enabled" />
                                            <spring:message code="settings.overtime.overtimeWritePrivilegedOnly.true"/>
                                        </label>
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeWritePrivilegedOnly.false"
                                                              path="overtimeSettings.overtimeWritePrivilegedOnly" value="false"
                                                              data-test-id="setting-overtime-write-privileged-only-disabled" />
                                            <spring:message code="settings.overtime.overtimeWritePrivilegedOnly.false"/>
                                        </label>
                                    </div>
                                </div>

                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                        <spring:message code='settings.overtime.overtimeReductionWithoutApplicationActive'/>:
                                    </label>
                                    <div class="col-md-8 radio">
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeReductionWithoutApplicationActive.true"
                                                              path="overtimeSettings.overtimeReductionWithoutApplicationActive" value="true"
                                                              data-test-id="setting-overtime-reduction-enabled" />
                                            <spring:message code="settings.overtime.overtimeReductionWithoutApplicationActive.true"/>
                                        </label>
                                        <label class="halves">
                                            <form:radiobutton id="overtimeSettings.overtimeReductionWithoutApplicationActive.false"
                                                              path="overtimeSettings.overtimeReductionWithoutApplicationActive" value="false"
                                                              data-test-id="setting-overtime-reduction-disabled" />
                                            <spring:message code="settings.overtime.overtimeReductionWithoutApplicationActive.false"/>
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
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="overtimeSettings.minimumOvertimeReduction">
                                        <spring:message code="settings.overtime.minimumOvertimeReduction"/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="overtimeSettings.minimumOvertimeReduction"
                                                    path="overtimeSettings.minimumOvertimeReduction" class="form-control"
                                                    cssErrorClass="form-control error"
                                                    type="number" step="1" />
                                        <uv:error-text>
                                            <form:errors path="overtimeSettings.minimumOvertimeReduction" />
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
                                                    type="submit" class="button-main col-md-3 col-md-push-4">
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
                        <button type="submit" class="button-main-green pull-left col-xs-12 col-sm-5 col-md-2" data-test-id="settings-save-button">
                            <spring:message code='action.save'/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

<footer>
    <div class="tw-mb-4">
        <span class="tw-text-sm">
            <a href="https://urlaubsverwaltung.cloud/">urlaubsverwaltung.cloud</a> |
        </span>
        <span class="tw-text-xs">
            v${version}
        </span>
    </div>
</footer>

</body>
</html>
