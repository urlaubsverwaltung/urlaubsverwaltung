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

        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-6">
                <uv:section-heading>
                    <jsp:attribute name="actions">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/${person.id}/edit" class="icon-link tw-px-1" aria-hidden="true" data-title="<spring:message code="action.edit"/>">
                                <icon:pencil className="tw-w-5 tw-h-5" />
                            </a>
                        </sec:authorize>
                    </jsp:attribute>
                    <jsp:body>
                        <h1>
                            <spring:message code="person.details.masterData.title"/>
                        </h1>
                    </jsp:body>
                </uv:section-heading>

                <uv:person person="${person}" cssClass="tw-mb-4 tw-h-32" />

                <uv:box className="tw-mb-8">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-500 tw-text-white">
                            <icon:key className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <ul class="tw-space-y-2 tw-text-sm">
                            <c:forEach items="${person.permissions}" var="role">
                                <li>
                                    <spring:message code="person.form.permissions.roles.${role}.description"/>
                                </li>
                            </c:forEach>
                        </ul>
                    </jsp:body>
                </uv:box>

                <uv:section-heading>
                    <h2>
                        <spring:message code="person.details.departments.title"/>
                    </h2>
                </uv:section-heading>
                <uv:box className="tw-mb-8">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-blue-500 tw-text-white">
                            <icon:user-group className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <c:choose>
                            <c:when test="${empty departments}">
                                <spring:message code="person.details.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <ul>
                                <c:forEach items="${departments}" var="department">
                                    <c:choose>
                                        <c:when test="${departmentHeadOfDepartments.contains( department )}">
                                            <li><c:out value="${department.name}"/> <spring:message code="person.details.departments.departmentHead"/></li>
                                        </c:when>
                                        <c:otherwise>
                                            <li><c:out value="${department.name}"/></li>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </jsp:body>
                </uv:box>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
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

                <uv:account-entitlement account="${account}" className="tw-h-32 tw-mb-8" />

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

                <uv:box className="tw-mb-4">
                        <jsp:attribute name="icon">
                            <uv:box-icon className="tw-bg-green-500 tw-text-white">
                                <icon:clock className="tw-w-8 tw-h-8" />
                            </uv:box-icon>
                        </jsp:attribute>
                    <jsp:body>
                        <span class="tw-text-sm">
                            <c:choose>
                                <c:when test="${workingTime != null}">
                                    <span class="tw-mb-2">
                                        <spring:message code="person.details.workingTime.validity"/>
                                        <span class="is-inline-block"><uv:date date="${workingTime.validFrom}"/></span>:
                                    </span>
                                    <ul>
                                    <c:if test="${workingTime.monday.duration > 0}">
                                        <li>
                                            <spring:message code="MONDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.tuesday.duration > 0}">
                                        <li>
                                            <spring:message code="TUESDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.wednesday.duration > 0}">
                                        <li>
                                            <spring:message code="WEDNESDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.thursday.duration > 0}">
                                        <li>
                                            <spring:message code="THURSDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.friday.duration > 0}">
                                        <li>
                                            <spring:message code="FRIDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.saturday.duration > 0}">
                                        <li>
                                            <spring:message code="SATURDAY"/>
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.sunday.duration > 0}">
                                        <li>
                                            <spring:message code="SUNDAY"/>
                                        </li>
                                    </c:if>
                                    </ul>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.details.workingTime.none'/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </jsp:body>
                </uv:box>

                <uv:box className="tw-mb-8">
                    <jsp:attribute name="icon">
                        <uv:box-icon className="tw-bg-green-500 tw-text-white">
                            <icon:map className="tw-w-8 tw-h-8" />
                        </uv:box-icon>
                    </jsp:attribute>
                    <jsp:body>
                        <span class="tw-text-sm">
                            <span class="tw-mb-2"><spring:message code="person.details.workingTime.federalState"/></span>
                            <spring:message code="federalState.${federalState}"/>
                        </span>
                    </jsp:body>
                </uv:box>
            </div>

        </div>
    </div>
</div>

</body>

</html>
