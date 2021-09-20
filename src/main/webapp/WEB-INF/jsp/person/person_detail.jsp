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
        <spring:message code="person.details.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
</head>

<body>

<uv:menu/>

<div class="content">
    <div class="container">

        <div class="feedback">
            <c:if test="${createSuccess}">
                <div class="alert alert-success">
                    <spring:message code="person.details.action.create.success"/>
                </div>
            </c:if>
            <c:if test="${updateSuccess}">
                <div class="alert alert-success">
                    <spring:message code="person.details.action.update.success"/>
                </div>
            </c:if>
        </div>

        <div class="tw-space-y-12 md:tw-space-y-0 md:tw-grid tw-gap-12 tw-grid-cols-1 md:tw-grid-cols-2">
            <div class="md:tw-col-start-1 md:grid-row-start-1">
                <uv:section-heading>
                    <h1>
                        <spring:message code="person.details.masterData.title"/>
                    </h1>
                </uv:section-heading>
                <uv:person person="${person}" cssClass="tw-mb-0 tw-border-none" noPadding="true" />
            </div>
            <div class="md:tw-col-start-1 md:tw-row-start-3 md:tw-row-span-2">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/${person.id}/permissions" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="person.details.permissions.title"/>
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
                        <dl class="tw-m-0">
                            <c:forEach items="${person.permissions}" var="role" varStatus="loop">
                                <dt class="tw-mb-2 tw-font-medium">
                                    <spring:message code="person.form.permissions.roles.${role}"/>
                                </dt>
                                <dd class="tw-text-sm ${not loop.last ? 'tw-mb-8' : ''}">
                                    <spring:message code="person.form.permissions.roles.${role}.description"/>
                                </dd>
                            </c:forEach>
                        </dl>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-1 md:tw-row-start-2">
                <uv:section-heading>
                    <h2>
                        <spring:message code="person.details.departments.title"/>
                    </h2>
                </uv:section-heading>
                <uv:box className="tw-mb-2 tw-border-none" noPadding="true">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-400 tw-text-white">
                            <icon:user-group className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <c:choose>
                            <c:when test="${empty departments}">
                                <spring:message code="person.details.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <ul class="tw-space-y-0.5 tw-text-sm">
                                    <c:forEach items="${departments}" var="department">

                                        <sec:authorize access="hasAuthority('OFFICE')">
                                            <c:set var="IS_OFFICE" value="${true}"/>
                                        </sec:authorize>

                                        <c:set var="departmentLink">
                                            <c:choose>
                                                <c:when test="${IS_OFFICE}">
                                                    <a href="${URL_PREFIX}/department/${department.id}/edit"><c:out value="${department.name}"/></a>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:out value="${department.name}"/>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:set>

                                        <c:choose>
                                            <c:when test="${departmentHeadOfDepartments.contains( department ) && secondStageAuthorityOfDepartments.contains( department )}">
                                                <li><c:out escapeXml="false" value="${departmentLink}"/> <spring:message code="person.details.departments.departmentHeadAndSecondStageAuthority"/></li>
                                            </c:when>
                                            <c:when test="${departmentHeadOfDepartments.contains( department )}">
                                                <li><c:out escapeXml="false" value="${departmentLink}"/> <spring:message code="person.details.departments.departmentHead"/></li>
                                            </c:when>
                                            <c:when test="${secondStageAuthorityOfDepartments.contains( department )}">
                                                <li><c:out escapeXml="false" value="${departmentLink}"/> <spring:message code="person.details.departments.secondStageAuthority"/></li>
                                            </c:when>
                                            <c:otherwise>
                                                <li><c:out escapeXml="false" value="${departmentLink}"/></li>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-2 md:tw-row-start-1">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/${person.id}/account?year=${param.year}" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="person.details.annualVacation.title"/>
                        </h2>
                        <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/person/${person.id}?year="/>
                    </jsp:body>
                </uv:section-heading>
                <uv:account-entitlement account="${account}" className="tw-mb-2 tw-border-none" noPadding="true" />
            </div>
            <div class="md:tw-col-start-2 md:tw-row-start-3">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/${person.id}/workingtime" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="person.details.workingTime.title"/>
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
                    <span class="tw-text-sm">
                        <c:choose>
                            <c:when test="${workingTime != null}">
                                <p class="tw-mb-2">
                                    <spring:message code="person.details.workingTime.validity"/>
                                    <span class="is-inline-block"><uv:date date="${workingTime.validFrom}"/></span>:
                                </p>
                                <table class="tw-flex">
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
                                                <c:if test="${workingTime.monday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.monday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.tuesday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.tuesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.wednesday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.wednesday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.thursday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.thursday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.friday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.friday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.saturday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.saturday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                            <td class="tw-flex-1 tw-flex tw-items-center tw-py-0.5">
                                                <c:if test="${workingTime.sunday.duration > 0}">
                                                    <icon:check-circle />
                                                </c:if>
                                                <span class="tw-sr-only">
                                                    <spring:message code="${workingTime.sunday.duration > 0 ? 'person.workingday.yes' : 'person.workingday.no'}" />
                                                </span>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </c:when>
                            <c:otherwise>
                                <spring:message code='person.details.workingTime.none'/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                    </jsp:body>
                </uv:box>
            </div>
            <div class="md:tw-col-start-2 md:tw-row-start-2">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/${person.id}/workingtime" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h2>
                            <spring:message code="person.details.federalState.title"/>
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
                        <p>
                            <span class="tw-block tw-mb-1 tw-text-sm">
                                <spring:message code="person.details.workingTime.federalState"/>
                            </span>
                            <span class="tw-text-base tw-font-medium">
                                <spring:message code="federalState.${federalState}"/>
                            </span>
                        </p>
                    </jsp:body>
                </uv:box>
            </div>
        </div>
    </div>
</div>
</body>
</html>
