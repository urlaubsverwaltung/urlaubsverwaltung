<%-- 
    Document   : left_days
    Created on : 08.02.2012, 18:14:05
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<%-- Decide if remaining vacation days are shown or not --%>
<c:choose>

    <c:when test="${account != null}">

        <c:choose>
            <%-- if current date is before April or remaining vacation days of user don't expire on 1st April, show the number of remaining vacation days --%>
            <c:when test="${april == 1 || (not account.remainingVacationDaysExpire && account.remainingVacationDays > 0.00)}">
                <c:set var="left" value="${account.vacationDays + account.remainingVacationDays}" />
                <c:choose>
                    <c:when test="${left <= 1.00 && left > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>

                <spring:message code="${numberOfDays}" arguments="${left}" />

                <c:choose>
                    <c:when test="${account.remainingVacationDays == null}">
                        <c:set var="remDays" value="0" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="remDays" value="${account.remainingVacationDays}"/>
                    </c:otherwise>
                </c:choose>

                <c:choose>
                    <c:when test="${remDays <= 1.00 && remDays > 0.50}">
                        <c:set var="remaining" value="remaining.sing"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="remaining" value="remaining.plural"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${remaining}" arguments="${remDays}" />
            </c:when>
            <%-- if current date is after April and remaining vacation days of user expire on 1st April, don't show number of remaining vacation days --%>
            <c:otherwise>
                <c:set var="left" value="${account.vacationDays}" />
                <c:choose>
                    <c:when test="${left <= 1.00 && left > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${left}" />
            </c:otherwise>
        </c:choose>
    </c:when>

    <c:otherwise>
        <spring:message code='not.specified' />
    </c:otherwise>  

</c:choose>
