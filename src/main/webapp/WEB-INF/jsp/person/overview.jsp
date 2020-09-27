<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="overview.header.title" arguments="${person.niceName}, ${year}"/>
    </title>
    <uv:custom-head/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.css' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;
    </script>
    <script defer src="<asset:url value="npm.date-fns.js" />"></script>
    <script defer src="<asset:url value="date-fns-localized.js" />"></script>
    <script defer src="<asset:url value="app_detail~app_form~person_overview.js" />"></script>
    <script defer src="<asset:url value='app_form~overtime_form~person_overview~sick_note_form.js' />"></script>
    <script defer src="<asset:url value="person_overview.js" />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="true"/>
</sec:authorize>

<uv:menu/>

<div class="print-info--only-portrait">
    <h4><spring:message code="print.info.portrait"/></h4>
</div>

<div class="content print--only-portrait">

    <div class="container">

        <div class="row tw-mb-4 lg:tw-mb-6">
            <div class="col-xs-12">
                <%@include file="include/overview_header.jsp" %>
            </div>
        </div>

        <div class="row tw-mb-12">
            <div class="col-xs-12">
                <div class="tw-flex tw-flex-wrap tw-space-y-8 lg:tw-space-y-0">
                    <div class="tw-w-full lg:tw-w-1/3">
                        <uv:person person="${person}" nameIsNoLink="${true}" cssClass="tw-border-none tw-p-0" />
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                        <uv:account-entitlement account="${account}" className="tw-border-none tw-p-0" />
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                        <uv:account-left
                            account="${account}"
                            vacationDaysLeft="${vacationDaysLeft}"
                            beforeApril="${beforeApril}"
                            className="tw-border-none tw-p-0"
                        />
                    </div>
                </div>
            </div>
        </div>

        <!-- Overtime -->
        <c:if test="${settings.workingTimeSettings.overtimeActive}">

            <uv:section-heading>
                <jsp:attribute name="actions">
                    <c:if test="${person.id == signedInUser.id || IS_OFFICE}">
                        <a href="${URL_PREFIX}/overtime/new?person=${person.id}" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.overtime.new"/>">
                            <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </c:if>
                    <a href="${URL_PREFIX}/overtime?person=${person.id}" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.overtime.list"/>">
                        <uv:icon-view-grid className="tw-w-5 tw-h-5" />
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
                    <div class="tw-flex tw-flex-wrap">
                        <div class="tw-w-full lg:tw-w-1/3">
                        </div>
                        <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3">
                            <uv:overtime-total hours="${overtimeTotal}" cssClass="tw-border-none tw-p-0" />
                        </div>
                        <div class="tw-w-full sm:tw-w-1/2 lg:tw-w-1/3 tw-mt-8 lg:tw-mt-0">
                            <uv:overtime-left hours="${overtimeLeft}" cssClass="tw-border-none tw-p-0" />
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Calendar -->
        <uv:section-heading>
            <jsp:attribute name="actions">
                <a class="icon-link tw-text-base tw-flex tw-items-center" aria-hidden="true" href="${URL_PREFIX}/calendars/share/persons/${personId}">
                    <uv:icon-calendar className="tw-w-5 tw-h-5" />
                    &nbsp;<spring:message code="overview.calendar.share.link.text" />
                </a>
            </jsp:attribute>
            <jsp:body>
                <h2 id="calendar">
                    <spring:message code="overview.calendar.title"/>
                </h2>
            </jsp:body>
        </uv:section-heading>
        <div class="row tw-mb-4 lg:tw-mb-12">
            <div class="col-xs-12">
                <div id="datepicker"></div>
            </div>
        </div>

        <!-- Vacation -->
        <uv:section-heading>
            <jsp:attribute name="actions">
                <c:choose>
                    <c:when test="${person.id == signedInUser.id}">
                        <a class="icon-link tw-px-1" href="${URL_PREFIX}/application/new" data-title="<spring:message code="action.apply.vacation"/>">
                            <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
                        </a>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${IS_OFFICE}">
                            <a class="icon-link  tw-px-1" href="${URL_PREFIX}/application/new?person=${person.id}" data-title="<spring:message code="action.apply.vacation"/>">
                                <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
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
                <div class="tw-flex tw-flex-wrap sm:tw-justify-around tw-space-y-8 lg:tw-space-y-0">
                <div class="tw-w-full sm:tw-w-1/2">
                    <div class="box tw-border-none tw-p-0">
                        <span class="box-icon-container">
                            <span class="box-icon tw-bg-yellow-500 tw-text-white">
                                <uv:icon-sun className="tw-w-8 tw-h-8" />
                            </span>
                        </span>
                        <div class="box-text tw-flex tw-flex-col">
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                <spring:message code="overview.vacations.holidayLeave.1" />
                            </span>
                            <span class="tw-my-1 tw-text-lg tw-font-medium">
                                <spring:message code="overview.vacations.holidayLeave.2" arguments="${holidayLeave}"/>
                            </span>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                <span class="tw-flex tw-items-center">
                                    <span class="tw-text-green-500 tw-flex tw-items-center">
                                        <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                    </span>
                                    &nbsp;<spring:message code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}"/>
                                </span>
                            </span>
                        </div>
                    </div>
                </div>
                <div class="tw-w-full sm:tw-w-1/2">
                    <div class="box tw-border-none tw-p-0">
                        <div class="box-icon-container">
                            <div class="box-icon tw-bg-yellow-500 tw-text-white">
                                <uv:icon-flag className="tw-w-8 tw-h-8" />
                            </div>
                        </div>
                        <div class="box-text tw-flex tw-flex-col">
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                <spring:message code="overview.vacations.otherLeave.1" />
                            </span>
                            <span class="tw-my-1 tw-text-lg tw-font-medium">
                                <spring:message code="overview.vacations.otherLeave.2" arguments="${otherLeave}"/>
                            </span>
                            <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                <span class="tw-flex tw-items-center">
                                    <span class="tw-text-green-500 tw-flex tw-items-center">
                                        <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                    </span>
                                    &nbsp;<spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}"/>
                                </span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>
            </div>
        </div>

        <div class="row tw-mb-4 lg:tw-mb-12">
            <div class="col-xs-12">
                <%@include file="include/overview_app_list.jsp" %>
            </div>
        </div>

        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">

            <uv:section-heading>
                <jsp:attribute name="actions">
                    <c:if test="${IS_OFFICE}">
                        <a class="icon-link tw-px-1" href="${URL_PREFIX}/sicknote/new?person=${person.id}" data-title="<spring:message code="action.apply.sicknote" />">
                            <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
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
                    <div class="tw-flex tw-flex-wrap sm:tw-justify-around tw-space-y-8 lg:tw-space-y-0">
                    <div class="tw-w-full sm:tw-w-1/2">
                        <div class="box tw-border-none tw-p-0">
                            <span class="box-icon-container">
                                <span class="box-icon tw-bg-red-600 tw-text-white">
                                    <uv:icon-medkit className="tw-w-8 tw-h-8" />
                                </span>
                            </span>
                            <span class="box-text tw-flex tw-flex-col">
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                    <spring:message code="overview.sicknotes.sickdays.1" />
                                </span>
                                <span class="tw-my-1 tw-text-lg tw-font-medium">
                                    <spring:message code="overview.sicknotes.sickdays.2" arguments="${sickDaysOverview.sickDays.days['TOTAL']}" />
                                </span>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                    <span class="tw-flex tw-items-center">
                                        <span class="tw-text-green-500 tw-flex tw-items-center">
                                            <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                        </span>
                                        &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysOverview.sickDays.days['WITH_AUB']}"/>
                                    </span>
                                </span>
                            </span>
                        </div>
                    </div>
                    <div class="tw-w-full sm:tw-w-1/2">
                        <div class="box tw-border-none tw-p-0">
                            <span class="box-icon-container">
                                <span class="box-icon tw-bg-red-600 tw-text-white">
                                    <uv:icon-child className="tw-w-8 tw-h-8" />
                                </span>
                            </span>
                            <span class="box-text tw-flex tw-flex-col">
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                    <spring:message code="overview.sicknotes.sickdays.child.1" />
                                </span>
                                <span class="tw-my-1 tw-text-lg tw-font-medium">
                                    <spring:message code="overview.sicknotes.sickdays.child.2" arguments="${sickDaysOverview.childSickDays.days['TOTAL']} "/>
                                </span>
                                <span class="tw-text-sm tw-text-black tw-text-opacity-75">
                                    <span class="tw-flex tw-items-center">
                                        <span class="tw-text-green-500 tw-flex tw-items-center">
                                            <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                        </span>
                                        &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysOverview.childSickDays.days['WITH_AUB']}"/>
                                    </span>
                                </span>
                            </span>
                        </div>
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


