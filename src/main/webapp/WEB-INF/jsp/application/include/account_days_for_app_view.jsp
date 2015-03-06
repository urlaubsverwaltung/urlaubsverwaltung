<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tr class="even">
    <td>
        <spring:message code='overview.entitlement.per.year' />
    </td>
    <td>
        <c:choose>

            <c:when test="${account != null}">
                <c:set var ="ent" value="${account.annualVacationDays}" />
                <c:choose>
                    <c:when test="${ent <= 1.00 && ent > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${ent}" />
            </c:when>

            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 

        </c:choose> 

    </td>
</tr>

<tr class="odd">
    <td>
        <spring:message code='overview.remaining.days.last.year' />
    </td>
    <td>
        <c:choose>
            <c:when test="${account != null}">
                <c:set var="remDays" value="${account.remainingVacationDays}" />
                <c:choose>
                    <c:when test="${remDays <= 1.00 && remDays > 0.50}">
                        <c:set var="numberOfDays" value="day"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${remDays}" />
            </c:when>

            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 

        </c:choose>    
    </td>
</tr>

<tr class="even">
    <td>
        <spring:message code='overview.valid.period' />
    </td>
    <td>
        <uv:date date="${account.validFrom}" /> <spring:message code='to' /> <uv:date date="${account.validTo}" />
    </td>
</tr>

<tr class="odd">
    <td>
        <spring:message code='overview.actual.entitlement' />
    </td>
    <td>
        <c:choose>

            <c:when test="${account != null}">
                <c:set var ="vacDays" value="${account.vacationDays}" />
                <c:choose>
                    <c:when test="${vacDays <= 1.00 && vacDays > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${vacDays}" />&nbsp;+
                <c:set var="remDays" value="${account.remainingVacationDays}" />
                <c:choose>
                    <c:when test="${remDays <= 1.00 && remDays > 0.50}">
                        <c:set var="numberOfDays" value="day"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${remDays}" />&nbsp;<spring:message code="remaining" />
            </c:when>

            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 

        </c:choose> 
    </td>
</tr>

<tr class="even">
    <td>
        <spring:message code="overview.left" />
    </td>
    <td>
        <c:choose>

            <c:when test="${account != null}">
                <c:set var ="left" value="${vacationDaysLeft.vacationDays}" />
                <c:choose>
                    <c:when test="${left <= 1.00 && left > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${left}" />
                <c:if test="${beforeApril}">
                +
                <c:set var="remLeftDays" value="${vacationDaysLeft.remainingVacationDays}" />
                <c:choose>
                    <c:when test="${remLeftDays <= 1.00 && remLeftDays > 0.50}">
                        <c:set var="numberOfDays" value="day"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${vacationDaysLeft.remainingVacationDaysNotExpiring}" />&nbsp;<spring:message code="remaining" />
                </c:if>
            </c:when>

            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 

        </c:choose> 
    </td>
</tr>
