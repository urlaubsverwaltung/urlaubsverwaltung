<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

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
                <a href="${URL_PREFIX}/absences" class="icon-link tw-px-1"
                   data-title="<spring:message code="action.applications.absences_overview"/>">
                    <icon:calendar className="tw-w-5 tw-h-5"/>
                </a>
                <sec:authorize access="hasAnyAuthority('DEPARTMENT_HEAD', 'SECOND_STAGE_AUTHORITY', 'BOSS', 'OFFICE')">
                <a href="${URL_PREFIX}/application/statistics" class="icon-link tw-px-1"
                   data-title="<spring:message code="action.applications.statistics"/>">
                    <icon:presentation-chart-bar className="tw-w-5 tw-h-5"/>
                </a>
                </sec:authorize>
                <uv:print/>
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
            <div class="col-xs-12 tw-mb-10">
                <c:choose>
                    <c:when test="${empty applications}">
                        <p><spring:message code="applications.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table selectable-table list-table-bt-0 tw-text-sm">
                            <tbody>
                            <c:forEach items="${applications}" var="application" varStatus="loopStatus">
                                <tr class="active" onclick="navigate('${URL_PREFIX}/application/${application.id}');">
                                    <td class="print:tw-hidden is-centered">
                                        <uv:avatar
                                            url="${application.person.avatarUrl}?d=mm&s=40"
                                            username="${application.person.name}"
                                            width="40px"
                                            height="40px"
                                            border="true"
                                        />
                                    </td>
                                    <td class="hidden-xs print:tw-table-cell">
                                        <span class="tw-block tw-text-lg tw-mb-1">
                                            <c:out value="${application.person.name}"/>
                                        </span>
                                        <span>
                                            <spring:message code="application.applier.applied"/>
                                        </span>
                                    </td>
                                    <td class="halves">
                                        <a class="tw-block tw-mb-1 tw-text-lg print:no-link ${application.vacationType.category}"
                                           href="${URL_PREFIX}/application/${application.id}">
                                            <c:choose>
                                                <c:when test="${not empty application.duration}">
                                                    <c:out value="${application.duration}" />
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${application.workDays}" />
                                                    <spring:message code="duration.days"/>
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                        <div>
                                            <c:out value="${application.durationOfAbsenceDescription}" />
                                        </div>
                                    </td>
                                    <td class="hidden-xs hidden-sm text-right print:tw-hidden">
                                        <div class="tw-flex tw-space-x-4 tw-justify-end print:tw-hidden">
                                            <c:if test="${application.editAllowed}">
                                                <a class="action-link tw-text-gray-900 tw-text-opacity-50"
                                                   href="${URL_PREFIX}/application/${application.id}/edit">
                                                    <icon:pencil className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                                    <spring:message code='action.edit'/>
                                                </a>
                                            </c:if>
                                            <c:if test="${application.approveAllowed}">
                                                <a class="action-link tw-text-gray-900 tw-text-opacity-50"
                                                   href="${URL_PREFIX}/application/${application.id}?action=allow&shortcut=true">
                                                    <icon:check className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                                    <c:choose>
                                                        <c:when test="${application.temporaryApproveAllowed}">
                                                            <spring:message code='action.temporary_allow'/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <spring:message code='action.allow'/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </a>
                                            </c:if>
                                            <c:if test="${application.rejectAllowed}">
                                                <a class="action-link tw-text-gray-900 tw-text-opacity-50"
                                                   href="${URL_PREFIX}/application/${application.id}?action=reject&shortcut=true">
                                                    <icon:ban className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                                    <spring:message code='action.reject'/>
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

        <uv:section-heading>
            <jsp:body>
                <h1 id="cancellation-requests">
                    <spring:message code="applications.cancellation_request"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">
            <div class="col-xs-12 tw-mb-10">
                <c:choose>
                    <c:when test="${empty applications_cancellation_request}">
                        <p><spring:message code="applications.cancellation_request.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table selectable-table list-table-bt-0 tw-text-sm">
                            <tbody>
                            <c:forEach items="${applications_cancellation_request}" var="application" varStatus="loopStatus">
                                <tr class="active"
                                    onclick="navigate('${URL_PREFIX}/application/${application.id}');">
                                    <td class="print:tw-hidden is-centered">
                                        <img
                                            src="<c:out value='${application.person.avatarUrl}?d=mm&s=40'/>"
                                            alt="<spring:message code="gravatar.alt" arguments="${application.person.name}"/>"
                                            class="gravatar tw-rounded-full"
                                            width="40px"
                                            height="40px"
                                            onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                        />
                                    </td>
                                    <td class="hidden-xs print:tw-table-cell">
                                        <span class="tw-block tw-text-lg tw-mb-1">
                                            <c:out value="${application.person.name}"/>
                                        </span>
                                        <span>
                                            <spring:message code="application.applier.applied"/>
                                        </span>
                                    </td>
                                    <td class="halves">
                                        <a class="tw-block tw-mb-1 tw-text-lg print:no-link ${application.vacationType.category}"
                                           href="${URL_PREFIX}/application/${application.id}">
                                            <c:choose>
                                                <c:when test="${not empty application.duration}">
                                                    <c:out value="${application.duration}" />
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${application.workDays}" />
                                                    <spring:message code="duration.days"/>
                                                    <spring:message code="${application.vacationType.messageKey}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </a>
                                        <div>
                                            <c:out value="${application.durationOfAbsenceDescription}" />
                                        </div>
                                    </td>
                                    <td class="hidden-xs hidden-sm text-right print:tw-hidden">
                                        <c:if test="${IS_OFFICE}">
                                            <div class="tw-flex tw-space-x-4 tw-justify-end print:tw-hidden">
                                                <a class="action-link tw-text-gray-900 tw-text-opacity-50"
                                                   href="${URL_PREFIX}/application/${application.id}?action=cancel&shortcut=true">
                                                    <icon:trash className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                                    <spring:message code='action.delete'/>
                                                </a>
                                                <a class="action-link tw-text-gray-900 tw-text-opacity-50"
                                                   href="${URL_PREFIX}/application/${application.id}?action=decline-cancellation-request&shortcut=true">
                                                    <icon:ban className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
                                                    <spring:message code="action.cancellationRequest"/>
                                                </a>
                                            </div>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <uv:section-heading>
            <jsp:body>
                <h1 id="holiday-replacement">
                    <spring:message code="applications.holiday_replacement"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">
            <div class="col-xs-12 tw-mb-10">
                <c:choose>
                    <c:when test="${empty applications_holiday_replacements}">
                        <p><spring:message code="applications.holiday_replacement.none"/></p>
                    </c:when>
                    <c:otherwise>
                        <table class="list-table list-table-bt-0 tw-text-sm">
                            <tbody>
                            <c:forEach items="${applications_holiday_replacements}" var="replacementInfo" varStatus="loopStatus">
                                <tr class="active">
                                    <td class="print:tw-hidden is-centered">
                                        <img
                                            src="<c:out value='${replacementInfo.person.avatarUrl}?d=mm&s=40'/>"
                                            alt="<spring:message code="gravatar.alt" arguments="${replacementInfo.person.name}"/>"
                                            class="gravatar tw-rounded-full"
                                            width="40px"
                                            height="40px"
                                            onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                        />
                                    </td>
                                    <td class="hidden-xs print:tw-table-cell">
                                        <span class="tw-block tw-text-lg tw-mb-1">
                                            <c:out value="${replacementInfo.person.name}"/>
                                        </span>
                                        <c:if test="${replacementInfo.pending}">
                                            <div>
                                                <spring:message code="applications.holiday_replacement.pending"/>
                                            </div>
                                        </c:if>
                                    </td>
                                    <td class="halves">
                                        <span class="tw-block tw-mb-1 tw-text-lg">
                                            <c:choose>
                                                <c:when test="${replacementInfo.duration != null}">
                                                    <c:out value="${replacementInfo.duration}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${replacementInfo.workDays}" />
                                                    <spring:message code="duration.days"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <div>
                                            <c:out value="${replacementInfo.durationOfAbsenceDescription}" />
                                        </div>
                                    </td>
                                    <td class="tw-break-words tw-max-w-xs">
                                        <c:out value="${replacementInfo.note}"/>
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
