<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="assets" uri = "/WEB-INF/assets.tld"%>

<assets:hash var="person_overview_css" asset="person_overview.css" />
<assets:hash var="npm_date_fn_js" asset="npm.date-fns.js" />
<assets:hash var="date_fn_localized_js" asset="date-fns-localized.js" />
<assets:hash var="person_overview_js" asset="person_overview.js" />

<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <link rel="stylesheet" href="<spring:url value='${person_overview_css}' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;
    </script>
    <script defer src="<spring:url value='${npm_date_fn_js}' />"></script>
    <script defer src="<spring:url value='${date_fn_localized_js}' />"></script>
    <script defer src="<spring:url value='${person_overview_js}' />"></script>
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

        <div class="row">

            <div class="col-xs-12">
                <%@include file="include/overview_header.jsp" %>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:person person="${person}" nameIsNoLink="${true}"/>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:account-entitlement account="${account}"/>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <uv:account-left account="${account}" vacationDaysLeft="${vacationDaysLeft}"
                                 beforeApril="${beforeApril}"/>
            </div>

        </div>

        <c:if test="${settings.workingTimeSettings.overtimeActive}">
            <div class="row">
                <div class="col-xs-12">
                    <legend>
                        <spring:message code="overtime.title"/>
                        <a href="${URL_PREFIX}/overtime?person=${person.id}"
                           class="fa-action pull-right" aria-hidden="true"
                           style="margin-top: 1px" data-title="<spring:message code="action.overtime.list"/>">
                            <i class="fa fa-th" aria-hidden="true"></i>
                        </a>
                        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">
                            <a href="${URL_PREFIX}/overtime/new?person=${person.id}"
                               class="fa-action pull-right" aria-hidden="true"
                               data-title="<spring:message code="action.overtime.new"/>">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:if>
                    </legend>
                </div>
                <div class="col-xs-12 col-md-6">
                    <uv:overtime-total hours="${overtimeTotal}"/>
                </div>
                <div class="col-xs-12 col-md-6">
                    <uv:overtime-left hours="${overtimeLeft}"/>
                </div>
            </div>
        </c:if>

        <div class="row">
            <div class="col-xs-12">
                <hr/>
                <div id="datepicker"></div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <legend id="vacation">
                    <spring:message code="applications.title"/>
                    <c:choose>
                        <c:when test="${person.id == signedInUser.id}">
                            <a class="fa-action pull-right" aria-hidden="true" href="${URL_PREFIX}/application/new"
                               data-title="<spring:message code="action.apply.vacation"/>">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <c:if test="${IS_OFFICE}">
                                <a class="fa-action pull-right" aria-hidden="true"
                                   href="${URL_PREFIX}/application/new?person=${person.id}"
                                   data-title="<spring:message code="action.apply.vacation"/>">
                                    <i class="fa fa-plus-circle" aria-hidden="true"></i>
                                </a>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                </legend>
            </div>
        </div>

        <div class="row">

            <c:set var="holidayLeave"
                   value="${usedDaysOverview.holidayDays.days['WAITING'] + usedDaysOverview.holidayDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="holidayLeaveAllowed" value="${usedDaysOverview.holidayDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeave"
                   value="${usedDaysOverview.otherDays.days['WAITING'] + usedDaysOverview.otherDays.days['TEMPORARY_ALLOWED'] + usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>
            <c:set var="otherLeaveAllowed" value="${usedDaysOverview.otherDays.days['ALLOWED'] + 0}"/>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow hidden-print">
                        <i class="fa fa-sun-o" aria-hidden="true"></i>
                    </span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.holidayLeave" arguments="${holidayLeave}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message
                        code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}"/>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow hidden-print"><i class="fa fa-flag-o" aria-hidden="true"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message code="overview.vacations.otherLeaveAllowed"
                                                                             arguments="${otherLeaveAllowed}"/>
                    </span>
                </div>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                <%@include file="include/overview_app_list.jsp" %>
            </div>

        </div>

        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">

            <div class="row">
                <div class="col-xs-12">
                    <legend id="anchorSickNotes">
                        <spring:message code="sicknotes.title"/>
                        <c:if test="${IS_OFFICE}">
                            <a class="fa-action pull-right" href="${URL_PREFIX}/sicknote/new?person=${person.id}"
                               data-title="<spring:message code="action.apply.sicknote" />">
                                <i class="fa fa-plus-circle" aria-hidden="true"></i>
                            </a>
                        </c:if>
                    </legend>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red hidden-print">
                            <i class="fa fa-medkit" aria-hidden="true"></i>
                        </span>
                        <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays"
                                        arguments="${sickDaysOverview.sickDays.days['TOTAL']}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i>
                        <spring:message code="overview.sicknotes.sickdays.aub"
                                        arguments="${sickDaysOverview.sickDays.days['WITH_AUB']}"/>
                    </span>
                    </div>
                </div>
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red hidden-print">
                            <i class="fa fa-child" aria-hidden="true"></i>
                        </span>
                        <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays.child"
                                        arguments="${sickDaysOverview.childSickDays.days['TOTAL']}"/>
                        <i class="fa fa-check positive" aria-hidden="true"></i>
                        <spring:message code="overview.sicknotes.sickdays.aub"
                                        arguments="${sickDaysOverview.childSickDays.days['WITH_AUB']}"/>
                    </span>
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


