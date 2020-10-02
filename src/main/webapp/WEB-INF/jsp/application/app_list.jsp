<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<sec:authorize access="hasAuthority('USER')">
    <c:set var="IS_USER" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('BOSS')">
    <c:set var="IS_BOSS" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('DEPARTMENT_HEAD')">
    <c:set var="IS_DEPARTMENT_HEAD" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('SECOND_STAGE_AUTHORITY')">
    <c:set var="IS_SECOND_STAGE_AUTHORITY" value="${true}"/>
</sec:authorize>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:set var="CAN_ALLOW" value="${IS_BOSS || IS_DEPARTMENT_HEAD || IS_SECOND_STAGE_AUTHORITY}"/>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="applications.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='app_list.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<uv:menu/>

<div class="content">

    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <sec:authorize access="hasAuthority('OFFICE')">
                <a href="${URL_PREFIX}/application/new" class="icon-link tw-px-1" data-title="<spring:message code="action.apply.vacation"/>">
                    <uv:icon-plus-circle className="tw-w-5 tw-h-5" />
                </a>
                </sec:authorize>
                <a href="${URL_PREFIX}/absences" class="icon-link tw-px-1" data-title="<spring:message code="action.applications.absences_overview"/>">
                    <uv:icon-calendar className="tw-w-5 tw-h-5" />
                </a>
                <a href="${URL_PREFIX}/application/statistics" class="icon-link tw-px-1" data-title="<spring:message code="action.applications.statistics"/>">
                    <uv:icon-presentation-chart-bar className="tw-w-5 tw-h-5" />
                </a>
                <uv:print />
            </jsp:attribute>
            <jsp:body>
                <h1>
                    <spring:message code="applications.waiting"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">
            <div class="col-xs-12">
                <div class="feedback">
                    <c:choose>
                        <c:when test="${allowSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.allow.success"/>
                            </div>
                        </c:when>
                        <c:when test="${temporaryAllowSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.temporary_allow.success"/>
                            </div>
                        </c:when>
                        <c:when test="${rejectSuccess}">
                            <div class="alert alert-success">
                                <spring:message code="application.action.reject.success"/>
                            </div>
                        </c:when>
                    </c:choose>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${empty applications}">
                        <p>
                            <spring:message code="applications.none"/>
                        </p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table selectable-table list-table-bt-0 tw-text-sm">
                            <tbody>
                            <c:forEach items="${applications}" var="application" varStatus="loopStatus">
                                <tr class="active" onclick="navigate('${URL_PREFIX}/application/${application.id}');">
                                    <td class="hidden-print is-centered">
                                        <img
                                            src="<c:out value='${application.person.gravatarURL}?d=mm&s=60'/>"
                                            alt="<spring:message code="gravatar.alt" arguments="${application.person.niceName}"/>"
                                            class="gravatar tw-rounded-full"
                                            width="60px"
                                            height="60px"
                                            onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                        />
                                    </td>
                                    <td class="hidden-xs">
                                        <span class="tw-block tw-text-lg tw-mb-1">
                                            <c:out value="${application.person.niceName}"/>
                                        </span>
                                        <span>
                                            <spring:message code="application.applier.applied"/>
                                        </span>
                                    </td>
                                    <td class="halves">
                                        <a class="tw-block tw-mb-1 tw-text-lg print:no-link ${application.vacationType.category}" href="${URL_PREFIX}/application/${application.id}">
                                            <c:choose>
                                                <c:when test="${application.hours != null}">
                                                    <uv:number number="${application.hours}"/>
                                                    <spring:message code="duration.hours"/>
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <uv:number number="${application.workDays}"/>
                                                    <spring:message code="duration.days"/>
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                        <div>
                                            <c:choose>
                                                <c:when test="${application.startDate == application.endDate}">
                                                    <c:set var="APPLICATION_DATE">
                                                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                                                        <uv:date date="${application.startDate}"/>
                                                    </c:set>
                                                    <c:choose>
                                                        <c:when
                                                            test="${application.startTime != null && application.endTime != null}">
                                                            <c:set var="APPLICATION_START_TIME">
                                                                <uv:time dateTime="${application.startDateWithTime}"/>
                                                            </c:set>
                                                            <c:set var="APPLICATION_END_TIME">
                                                                <uv:time dateTime="${application.endDateWithTime}"/>
                                                            </c:set>
                                                            <c:set var="APPLICATION_TIME">
                                                                <spring:message code="absence.period.time"
                                                                                arguments="${APPLICATION_START_TIME};${APPLICATION_END_TIME}"
                                                                                argumentSeparator=";"/>
                                                            </c:set>
                                                            <spring:message code="absence.period.singleDay"
                                                                            arguments="${APPLICATION_DATE};${APPLICATION_TIME}"
                                                                            argumentSeparator=";"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:set var="APPLICATION_DAY_LENGTH">
                                                                <spring:message code="${application.dayLength}"/>
                                                            </c:set>
                                                            <spring:message code="absence.period.singleDay"
                                                                            arguments="${APPLICATION_DATE};${APPLICATION_DAY_LENGTH}"
                                                                            argumentSeparator=";"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="APPLICATION_START_DATE">
                                                        <spring:message code="${application.weekDayOfStartDate}.short"/>,
                                                        <uv:date date="${application.startDate}"/>
                                                    </c:set>
                                                    <c:set var="APPLICATION_END_DATE">
                                                        <spring:message code="${application.weekDayOfEndDate}.short"/>,
                                                        <uv:date date="${application.endDate}"/>
                                                    </c:set>
                                                    <spring:message code="absence.period.multipleDays"
                                                                    arguments="${APPLICATION_START_DATE};${APPLICATION_END_DATE}"
                                                                    argumentSeparator=";"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </td>
                                    <td class="hidden-xs hidden-sm text-right">
                                        <div class="print:tw-hidden">
                                        <c:if
                                            test="${CAN_ALLOW && (application.person.id != signedInUser.id || IS_BOSS)}">
                                            <a class="icon-link tw-p-1 hover:tw-text-green-500" href="${URL_PREFIX}/application/${application.id}?action=allow&shortcut=true" data-title="<spring:message code='action.allow'/>">
                                                <uv:icon-check className="tw-w-5 tw-h-5" solid="true" />
                                            </a>
                                        </c:if>
                                        <c:if
                                            test="${CAN_ALLOW && (application.person.id != signedInUser.id || IS_BOSS)}">
                                            <a class="icon-link tw-p-1 hover:tw-text-red-500" href="${URL_PREFIX}/application/${application.id}?action=reject&shortcut=true" data-title="<spring:message code='action.reject'/>">
                                                <uv:icon-ban className="tw-w-5 tw-h-5" solid="true" />
                                            </a>
                                        </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>
</div>

</body>

</html>
