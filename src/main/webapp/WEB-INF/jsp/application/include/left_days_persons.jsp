<%-- 
    Document   : left_days_persons
    Created on : 13.02.2012, 17:50:21
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<%-- Decide if remaining vacation days are shown or not --%>
<c:choose>

    <c:when test="${accounts[person] != null}">

        <c:choose>
            <%-- if current date is before April OR remaining vacation days of user don't expire on 1st April AND only if number of days is greater than 0, show the number of remaining vacation days --%>
            <c:when test="${(april == 1 || not accounts[person].remainingVacationDaysExpire) && accounts[person].remainingVacationDays > 0.00}">
                <c:set var="left" value="${accounts[person].vacationDays + accounts[person].remainingVacationDays}" />
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
                    <c:when test="${accounts[person].remainingVacationDays != null}">
                        <c:set var="remDays" value="${accounts[person].remainingVacationDays}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="remDays" value="0"/>
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

            <%-- if current date is after April and remaining vacation days of user expire on 1st April OR if number of days is not greater than 0, don't show number of remaining vacation days --%>
            <c:otherwise>
                <c:set var="left" value="${accounts[person].vacationDays}" />
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
