<%-- 
    Document   : account_days
    Created on : 08.02.2012, 18:31:36
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<p>
    <%--<spring:message code='overview.actual.entitlement'/>--%>
    Anspruch:
    <c:choose>

        <c:when test="${account != null}">
            <c:set var="vacDays" value="${account.vacationDays}"/>
            <c:choose>
                <c:when test="${vacDays <= 1.00 && vacDays > 0.50}">
                    <c:set var="numberOfDays" value="day"/>
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days"/>
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${vacDays}"/>&nbsp;+
            <c:set var="remDays" value="${account.remainingVacationDays}"/>
            <c:choose>
                <c:when test="${remDays <= 1.00 && remDays > 0.50}">
                    <c:set var="numberOfDays" value="day"/>
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days"/>
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${remDays}"/>&nbsp;<spring:message code="remaining"/>
        </c:when>

        <c:otherwise>
            <spring:message code='not.specified'/>
        </c:otherwise>

    </c:choose>
</p>

<p>
    Verbleibend:
    <%--<spring:message code="overview.left"/>--%>
    <c:choose>

        <c:when test="${account != null}">
            <c:set var="left" value="${leftDays}"/>
            <c:choose>
                <c:when test="${left <= 1.00 && left > 0.50}">
                    <c:set var="numberOfDays" value="day"/>
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days"/>
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${left}"/>
            <c:if test="${beforeApril || !account.remainingVacationDaysExpire}">
                +
                <c:set var="remLeftDays" value="${remLeftDays}"/>
                <c:choose>
                    <c:when test="${remLeftDays <= 1.00 && remLeftDays > 0.50}">
                        <c:set var="numberOfDays" value="day"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${remLeftDays}"/>&nbsp;<spring:message
                    code="remaining"/>
            </c:if>
        </c:when>

        <c:otherwise>
            <spring:message code='not.specified'/>
        </c:otherwise>

    </c:choose>
</p>