<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
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

        <div class="row">
            <div class="col-xs-12">
                <legend><spring:message code="person.form.data.title"/></legend>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-6">
                <uv:person person="${person}"/>
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-key"></i></span>
                    <span class="box-text">
                        <h5><spring:message code="person.form.permissions.roles"/></h5>
                        <ul class="fa-ul">
                            <c:forEach items="${person.permissions}" var="role">
                                <li>
                                    <p>
                                        <i class="fa-li fa fa-angle-right"></i>
                                        <spring:message code="person.form.permissions.roles.${role}"/>
                                    </p>
                                </li>
                            </c:forEach>
                        </ul>
                    </span>
                </div>
                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-group"></i></span>
                    <span class="box-text">
                        <h5><spring:message code="person.form.departments.title"/></h5>
                        <c:choose>
                            <c:when test="${empty departments}">
                                <spring:message code="person.form.departments.none"/>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${departments}" var="department">
                                    <c:choose>
                                        <c:when test="${not empty department.description}">
                                            <div>
                                                <div class="overflow" data-toggle="popover"
                                                     data-trigger="hover"
                                                     data-placement="right"
                                                     title="<spring:message code='department.data.info'/>"
                                                     data-content="${department.description}">
                                                    <c:out value="${department.name}"/>
                                                    <i class="fa fa-fw fa-info-circle hidden-print"></i>
                                                </div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div><c:out value="${department.name}"/></div>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <uv:account-entitlement account="${account}"/>
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-clock-o"></i></span>
                    <span class="box-text">
                        <h5><spring:message code="person.form.workingTime.title"/></h5>
                        <c:forEach items="${workingTimes}" var="workingTime">
                            <p>
                                <spring:message code="person.form.workingTime.validityPeriod" />
                                <uv:date date="${workingTime.validFrom}" />
                                <br />
                                <c:if test="${workingTime.monday.duration > 0}">
                                    <spring:message code="MONDAY" />
                                </c:if>
                                <c:if test="${workingTime.tuesday.duration > 0}">
                                    <spring:message code="TUESDAY" />
                                </c:if>
                                <c:if test="${workingTime.wednesday.duration > 0}">
                                    <spring:message code="WEDNESDAY" />
                                </c:if>
                                <c:if test="${workingTime.thursday.duration > 0}">
                                    <spring:message code="THURSDAY" />
                                </c:if>
                                <c:if test="${workingTime.friday.duration > 0}">
                                    <spring:message code="FRIDAY" />
                                </c:if>
                                <c:if test="${workingTime.saturday.duration > 0}">
                                    <spring:message code="SATURDAY" />
                                </c:if>
                                <c:if test="${workingTime.sunday.duration > 0}">
                                    <spring:message code="SUNDAY" />
                                </c:if>
                            </p>
                        </c:forEach>
                    </span>
                </div>
            </div>

        </div>
    </div>
</div>

</body>

</html>
