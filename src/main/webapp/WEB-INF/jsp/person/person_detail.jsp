<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


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

            <div class="col-xs-12 col-sm-12 col-md-6">
                <legend><spring:message code="person.form.data.title"/></legend>
                <uv:person person="${person}"/>

                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-key"></i></span>
                    <span class="box-text">
                        <h5><spring:message code="person.form.permissions.roles"/></h5>
                        <c:forEach items="${person.permissions}" var="role">
                            <p>
                                <spring:message code="person.form.permissions.roles.${role}"/>:
                                <spring:message code="person.form.permissions.roles.${role}.description"/>
                            </p>
                        </c:forEach>
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

                <div class="box">
                    <span class="box-icon bg-blue"><i class="fa fa-clock-o"></i></span>
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

            <div class="col-xs-12 col-sm-12 col-md-6">
                    <legend>
                        <spring:message code="person.account.vacation.title"/>
                        <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/staff/${person.id}?year="/>
                    </legend>

                    <div class="box">
                        <span class="box-icon bg-green"><i class="fa fa-calendar"></i></span>
                        <span class="box-text">
                            <c:choose>
                                <c:when test="${account != null}">
                                    <spring:message code="person.account.vacation.entitlement" arguments="${account.vacationDays}" />
                                    <spring:message code="person.account.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.account.vacation.noInformation'/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="box">
                        <span class="box-icon bg-green"><i class="fa fa-bar-chart"></i></span>
                        <span class="box-text">
                            <c:choose>
                                <c:when test="${account != null}">
                                    <spring:message code="person.account.vacation.left" arguments="${vacationDaysLeft.vacationDays}" />
                                    <c:choose>
                                        <c:when test="${beforeApril}">
                                            <spring:message code="person.account.vacation.left.remaining" arguments="${vacationDaysLeft.remainingVacationDays}" />
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message code="person.account.vacation.left.remaining" arguments="${vacationDaysLeft.remainingVacationDaysNotExpiring}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.account.vacation.noInformation'/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>

                    <div class="box">
                        <span class="box-icon bg-green"><i class="fa fa-flash"></i></span>
                        <span class="box-text">
                            <c:choose>
                                <c:when test="${account != null}">
                                    <spring:message code="person.account.vacation.entitlement.remaining.notExpiring" arguments="${vacationDaysLeft.remainingVacationDaysNotExpiring}" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code='person.account.vacation.noInformation'/>
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
            </div>

        </div>
    </div>
</div>

</body>

</html>
