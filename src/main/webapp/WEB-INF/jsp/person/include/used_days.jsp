<%-- 
    Document   : used_days
    Created on : 05.09.2012, 14:07:21
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tr class="odd">
    <th><spring:message code='vac.holiday' /></th>
    <td>
        <c:set var ="holidayDays" value="${numberOfHolidayDays + 0}" />
            <c:choose>
                <c:when test="${holidayDays <= 1.00 && holidayDays > 0.50}">
                    <c:set var="numberOfDays" value="day" />
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days" />
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${holidayDays}" />
    </td>
</tr>

<tr class="even">
    <th><spring:message code='vac.special' /></th>
    <td>
        <c:set var ="specialDays" value="${numberOfSpecialLeaveDays + 0}" />
            <c:choose>
                <c:when test="${specialDays <= 1.00 && specialDays > 0.50}">
                    <c:set var="numberOfDays" value="day" />
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days" />
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${specialDays}" />
    </td>
</tr>

<tr class="odd">
    <th><spring:message code='vac.unpaid' /></th>
    <td>
        <c:set var ="unpaidDays" value="${numberOfUnpaidLeaveDays + 0}" />
            <c:choose>
                <c:when test="${unpaidDays <= 1.00 && unpaidDays > 0.50}">
                    <c:set var="numberOfDays" value="day" />
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days" />
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${unpaidDays}" />
    </td>
</tr>

<tr class="even">
    <th><spring:message code='vac.overtime' /></th>
    <td>
        <c:set var ="overtimeDays" value="${numberOfOvertimeDays + 0}" />
            <c:choose>
                <c:when test="${overtimeDays <= 1.00 && overtimeDays > 0.50}">
                    <c:set var="numberOfDays" value="day" />
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days" />
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${overtimeDays}" />
    </td>
</tr>

<tr class="odd">
    <th style="border-top: 2px solid #D7D7D7">Gesamt</th>
    <td style="border-top: 2px solid #D7D7D7">
        <c:set var ="allDays" value="${numberOfHolidayDays + numberOfSpecialLeaveDays + numberOfUnpaidLeaveDays + numberOfOvertimeDays}" />
            <c:choose>
                <c:when test="${allDays <= 1.00 && allDays > 0.50}">
                    <c:set var="numberOfDays" value="day" />
                </c:when>
                <c:otherwise>
                    <c:set var="numberOfDays" value="days" />
                </c:otherwise>
            </c:choose>
            <spring:message code="${numberOfDays}" arguments="${allDays}" />
    </td>
</tr>
