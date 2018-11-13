<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html>
<html>

<spring:url var="URL_PREFIX" value="/web"/>

<head>
    <uv:head/>
</head>

<body>

<uv:menu/>

<div class="content">
    <div class="container">

        <div class="feedback">
            <c:if test="${createSuccess}">
                <div class="alert alert-success">
                    <spring:message code="person.details.action.create.success" />
                </div>
            </c:if>
            <c:if test="${updateSuccess}">
                <div class="alert alert-success">
                    <spring:message code="person.details.action.update.success" />
                </div>
            </c:if>
        </div>

        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-6">
                <legend>
                    <spring:message code="person.details.masterData.title"/>
                    <sec:authorize access="hasRole('OFFICE')">
                    <a href="${URL_PREFIX}/staff/${person.id}/edit" class="fa-action pull-right"
                       data-title="<spring:message code="action.edit"/>">
                        <i class="fa fa-pencil"></i>
                    </a>
                    </sec:authorize>
                </legend>
                <uv:person person="${person}"/>
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-key"></i></span>
                    <span class="box-text">
                        <ul>
                            <c:forEach items="${person.permissions}" var="role">
                                <li>
                                    <p><spring:message code="person.form.permissions.roles.${role}"/></p>
                                </li>
                            </c:forEach>
                        </ul>
                    </span>
                </div>

                <legend><spring:message code="person.details.departments.title"/></legend>
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-group"></i></span>
                    <span class="box-text">
                        <c:choose>
                            <c:when test="${empty departments}">
                                <spring:message code="person.details.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <ul>
                                <c:forEach items="${departments}" var="department">
                                    <li><c:out value="${department.name}"/></li>
                                </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <legend>
                    <spring:message code="person.details.annualVacation.title"/>
                    <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/staff/${person.id}?year="/>
                    <sec:authorize access="hasRole('OFFICE')">
                    <a href="${URL_PREFIX}/staff/${person.id}/account?year=${param.year}" class="fa-action pull-right"
                       data-title="<spring:message code="action.edit"/>">
                        <i class="fa fa-pencil"></i>
                    </a>
                    </sec:authorize>
                </legend>
                <uv:account-entitlement account="${account}"/>

                <legend>
                    <spring:message code="person.details.workingTime.title"/>
                    <sec:authorize access="hasRole('OFFICE')">
                    <a href="${URL_PREFIX}/staff/${person.id}/workingtime" class="fa-action pull-right"
                       data-title="<spring:message code="action.edit"/>">
                        <i class="fa fa-pencil"></i>
                    </a>
                    </sec:authorize>
                </legend>
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-clock-o"></i></span>
                    <span class="box-text">
                            <c:choose>
                                <c:when test="${workingTime != null}">
                                    <spring:message code="person.details.workingTime.validity" />
                                    <h5 class="is-inline-block"><uv:date date="${workingTime.validFrom}" /></h5>:
                                    <ul>
                                    <c:if test="${workingTime.monday.duration > 0}">
                                        <li>
                                            <spring:message code="MONDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.tuesday.duration > 0}">
                                        <li>
                                            <spring:message code="TUESDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.wednesday.duration > 0}">
                                        <li>
                                            <spring:message code="WEDNESDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.thursday.duration > 0}">
                                        <li>
                                            <spring:message code="THURSDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.friday.duration > 0}">
                                        <li>
                                            <spring:message code="FRIDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.saturday.duration > 0}">
                                        <li>
                                            <spring:message code="SATURDAY" />
                                        </li>
                                    </c:if>
                                    <c:if test="${workingTime.sunday.duration > 0}">
                                        <li>
                                            <spring:message code="SUNDAY" />
                                        </li>
                                    </c:if>
                                    </ul>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.details.workingTime.none'/>
                                </c:otherwise>
                            </c:choose>
                    </span>
                </div>
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-map"></i></span>
                    <span class="box-text">
                        <spring:message code="person.details.workingTime.federalState" />
                        <h5><spring:message code="federalState.${federalState}"/></h5>
                    </span>
                </div>
            </div>

        </div>
    </div>
</div>

</body>

</html>
