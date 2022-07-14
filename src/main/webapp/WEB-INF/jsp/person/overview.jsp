<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<c:choose>
    <c:when test="${!empty param.year}">
        <c:set var="displayYear" value="${param.year}"/>
    </c:when>
    <c:otherwise>
        <c:set var="displayYear" value="${year}"/>
    </c:otherwise>
</c:choose>

<c:if test="${not empty param.year && param.year ne year}">
    <c:set var="yearUrlParameterFirst" scope="page" value="?year=${displayYear}" />
    <c:set var="yearUrlParameter" scope="page" value="&year=${displayYear}" />
</c:if>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">

<head>
    <title>
        <spring:message code="overview.header.title" arguments="${person.niceName}, ${displayYear}"/>
    </title>
    <uv:custom-head/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;
    </script>
    <uv:vacation-type-colors-script />
    <script defer src="<asset:url value="npm.date-fns.js" />"></script>
    <script defer src="<asset:url value="app_detail~app_form~person_overview.js" />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~704d57c1.js' />"></script>
    <script defer src="<asset:url value="person_overview.js" />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">

    <div class="container">

        <div class="row">
            <div class="col-xs-12">
                <%@include file="include/overview_header.jsp" %>
            </div>
        </div>

        <div class="row tw-mb-12">
            <div class="col-xs-12">
                <div class="tw-flex tw-flex-wrap tw-space-y-8 lg:tw-space-y-0">
                    <div class="tw-w-full lg:tw-w-1/3">
                        <uv:person-box-narrow person="${person}" departmentsOfPerson="${departments}" nameIsNoLink="${true}" cssClass="tw-border-none" />
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                        <uv:account-entitlement-box-narrow account="${account}" className="tw-border-none" />
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                        <uv:account-left-box-narrow
                            account="${account}"
                            vacationDaysLeft="${vacationDaysLeft}"
                            beforeExpiryDate="${isBeforeExpiryDate}"
                            className="tw-border-none"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- Overtime -->
        <c:if test="${settings.overtimeSettings.overtimeActive}">

            <uv:section-heading>
                <jsp:attribute name="actions">
                    <c:if test="${userIsAllowedToWriteOvertime}">
                        <a href="${URL_PREFIX}/overtime/new?person=${person.id}" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.overtime.new"/>">
                            <icon:plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </c:if>
                    <a href="${URL_PREFIX}/overtime?person=${person.id}${yearUrlParameter}" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.overtime.list"/>">
                        <icon:view-grid className="tw-w-5 tw-h-5" />
                    </a>
                </jsp:attribute>
                <jsp:body>
                    <h2 id="overtime">
                        <spring:message code="overtime.title"/>
                    </h2>
                </jsp:body>
            </uv:section-heading>

            <div class="row tw-mb-12">
                <div class="col-xs-12">
                    <div class="tw-flex tw-flex-wrap tw-space-y-8 lg:tw-space-y-0">
                        <div class="tw-w-full lg:tw-w-1/3">
                        </div>
                        <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                            <uv:overtime-total-box-narrow hours="${overtimeTotal}" cssClass="tw-border-none" />
                        </div>
                        <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                            <uv:overtime-left-box-narrow hours="${overtimeLeft}" cssClass="tw-border-none tw-p-0" />
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Calendar -->
        <div class="print:tw-hidden">

            <uv:section-heading>
                <jsp:attribute name="actions">
                <div class="tw-flex tw-flex-wrap tw-justify-end tw-space-y-2 sm:tw-space-y-0 sm:tw-space-x-2">
                    <c:if test="${canAccessAbsenceOverview}">
                        <a class="icon-link tw-text-base tw-flex tw-items-center tw-flex-row-reverse tw-space-x-1 sm:tw-space-x-0 sm:tw-flex-row tw-mr-0"
                           aria-hidden="true" href="${URL_PREFIX}/absences">
                            <icon:calendar className="tw-w-5 tw-h-5"/>
                            &nbsp;<spring:message code="overview.absences.overview.link.text"/>
                        </a>
                    </c:if>
                    <c:if test="${canAccessCalendarShare}">
                        <a class="icon-link tw-text-base tw-flex tw-items-center tw-flex-row-reverse tw-space-x-1 sm:tw-space-x-0 sm:tw-flex-row"
                           aria-hidden="true" href="${URL_PREFIX}/calendars/share/persons/${person.id}">
                            <icon:share className="tw-w-5 tw-h-5"/>
                            &nbsp;<spring:message code="overview.calendar.share.link.text"/>
                        </a>
                    </c:if>
                </div>
                </jsp:attribute>
                <jsp:body>
                    <h2 id="calendar">
                        <spring:message code="overview.calendar.title"/>
                    </h2>
                </jsp:body>
            </uv:section-heading>
            <div class="row tw-mb-4 lg:tw-mb-12">
                <div class="col-xs-12">
                    <div id="datepicker" class="tw-flex tw-justify-center tw-items-center">
                        <div class="lds-ellipsis"><div></div><div></div><div></div><div></div></div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Vacation -->
        <uv:section-heading>
            <jsp:attribute name="actions">
                <c:choose>
                    <c:when test="${person.id == signedInUser.id}">
                        <a class="icon-link tw-px-1" href="${URL_PREFIX}/application/new" data-title="<spring:message code="action.apply.vacation"/>">
                            <icon:plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${canAddApplicationForLeaveForAnotherUser}">
                            <a class="icon-link  tw-px-1" href="${URL_PREFIX}/application/new?person=${person.id}" data-title="<spring:message code="action.apply.vacation"/>">
                                <icon:plus-circle className="tw-w-5 tw-h-5" />
                            </a>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </jsp:attribute>
            <jsp:body>
                <h2 id="vacation">
                    <spring:message code="applications.title"/>
                </h2>
            </jsp:body>
        </uv:section-heading>

        <div class="row tw-mb-4 lg:tw-mb-6">

            <c:set var="holidayLeave"
                   value="${usedDaysOverview.holidayDays.days['WAITING'] + usedDaysOverview.holidayDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="holidayLeaveAllowed" value="${usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeave"
                   value="${usedDaysOverview.otherDays.days['WAITING'] + usedDaysOverview.otherDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeaveAllowed" value="${usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>

            <div class="col-xs-12">
                <div class="tw-flex tw-flex-wrap tw-space-y-8 sm:tw-space-y-0">
                <div class="tw-w-full sm:tw-w-1/2">
                    <uv:box-narrow className="tw-border-none">
                        <jsp:attribute name="icon">
                            <uv:box-icon className="tw-bg-amber-300 tw-text-white dark:tw-bg-amber-400 dark:tw-text-zinc-900">
                                <icon:sun className="tw-w-8 tw-h-8" />
                            </uv:box-icon>
                        </jsp:attribute>
                        <jsp:body>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                <spring:message code="overview.vacations.holidayLeave.1" />
                            </span>
                            <span class="tw-my-1 tw-text-lg tw-font-medium">
                                <spring:message code="overview.vacations.holidayLeave.2" arguments="${holidayLeave}"/>
                            </span>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                <span class="tw-flex tw-items-center">
                                    <span class="tw-text-emerald-500 tw-flex tw-items-center">
                                        <icon:check className="tw-w-5 tw-h-5" solid="true" />
                                    </span>
                                    &nbsp;<spring:message code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}"/>
                                </span>
                            </span>
                        </jsp:body>
                    </uv:box-narrow>
                </div>
                <div class="tw-w-full sm:tw-w-1/2">
                    <uv:box-narrow className="tw-border-none">
                        <jsp:attribute name="icon">
                            <uv:box-icon className="tw-bg-amber-300 tw-text-white dark:tw-bg-amber-400 dark:tw-text-zinc-900">
                                <icon:flag className="tw-w-8 tw-h-8" />
                            </uv:box-icon>
                        </jsp:attribute>
                        <jsp:body>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                <spring:message code="overview.vacations.otherLeave.1" />
                            </span>
                            <span class="tw-my-1 tw-text-lg tw-font-medium">
                                <spring:message code="overview.vacations.otherLeave.2" arguments="${otherLeave}"/>
                            </span>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                <span class="tw-flex tw-items-center">
                                    <span class="tw-text-emerald-500 tw-flex tw-items-center">
                                        <icon:check className="tw-w-5 tw-h-5" solid="true" />
                                    </span>
                                    &nbsp;<spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}"/>
                                </span>
                            </span>
                        </jsp:body>
                    </uv:box-narrow>
                </div>
            </div>
            </div>
        </div>

        <div class="row tw-mb-4 lg:tw-mb-12">
            <div class="col-xs-12">
                <%@include file="include/overview_app_list.jsp" %>
            </div>
        </div>

        <!-- sick note -->
        <c:if test="${person.id == signedInUser.id || canAddSickNoteAnotherUser}">

            <uv:section-heading>
                <jsp:attribute name="actions">
                    <c:if test="${canAddSickNoteAnotherUser}">
                        <a class="icon-link tw-px-1" href="${URL_PREFIX}/sicknote/new?person=${person.id}" data-title="<spring:message code="action.apply.sicknote" />">
                            <icon:plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </c:if>
                </jsp:attribute>
                <jsp:body>
                    <h2 id="anchorSickNotes">
                        <spring:message code="sicknotes.title"/>
                    </h2>
                </jsp:body>
            </uv:section-heading>

            <div class="row tw-mb-4 lg:tw-mb-6">
                <div class="col-xs-12">
                    <div class="tw-flex tw-flex-wrap tw-space-y-8 sm:tw-space-y-0">
                    <div class="tw-w-full sm:tw-w-1/2">
                        <uv:box-narrow className="tw-border-none">
                            <jsp:attribute name="icon">
                                <uv:box-icon className="tw-bg-red-500 tw-text-white dark:tw-bg-red-600 dark:tw-text-zinc-900">
                                    <icon:medkit className="tw-w-8 tw-h-8" />
                                </uv:box-icon>
                            </jsp:attribute>
                            <jsp:body>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                    <spring:message code="overview.sicknotes.sickdays.1" />
                                </span>
                                <span class="tw-my-1 tw-text-lg tw-font-medium">
                                    <spring:message code="overview.sicknotes.sickdays.2" arguments="${sickDaysOverview.sickDays.days['TOTAL']}" />
                                </span>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                    <span class="tw-flex tw-items-center">
                                        <span class="tw-text-emerald-500 tw-flex tw-items-center">
                                            <icon:check className="tw-w-5 tw-h-5" solid="true" />
                                        </span>
                                        &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysOverview.sickDays.days['WITH_AUB']}"/>
                                    </span>
                                </span>
                            </jsp:body>
                        </uv:box-narrow>
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2">
                        <uv:box-narrow className="tw-border-none">
                            <jsp:attribute name="icon">
                                <uv:box-icon className="tw-bg-red-500 tw-text-white dark:tw-bg-red-600 dark:tw-text-zinc-900">
                                    <icon:child className="tw-w-8 tw-h-8" />
                                </uv:box-icon>
                            </jsp:attribute>
                            <jsp:body>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                    <spring:message code="overview.sicknotes.sickdays.child.1" />
                                </span>
                                <span class="tw-my-1 tw-text-lg tw-font-medium">
                                    <spring:message code="overview.sicknotes.sickdays.child.2" arguments="${sickDaysOverview.childSickDays.days['TOTAL']} "/>
                                </span>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75 dark:tw-text-zinc-300 dark:tw-text-opacity-100">
                                    <span class="tw-flex tw-items-center">
                                        <span class="tw-text-emerald-500 tw-flex tw-items-center">
                                            <icon:check className="tw-w-5 tw-h-5" solid="true" />
                                        </span>
                                        &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysOverview.childSickDays.days['WITH_AUB']}"/>
                                    </span>
                                </span>
                            </jsp:body>
                        </uv:box-narrow>
                    </div>
                </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <%@include file="include/sick_notes.jsp" %>
                </div>
            </div>

        </c:if>

    </div>
</div>
</body>
</html>
