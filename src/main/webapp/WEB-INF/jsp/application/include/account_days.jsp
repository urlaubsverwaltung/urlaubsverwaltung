<%-- 
    Document   : account_days
    Created on : 08.02.2012, 18:31:36
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<tr class="odd">
    <th>
        <spring:message code='overview.entitlement.per.year' />
    </th>
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

<tr class="even">
    <th>
        <spring:message code='overview.remaining.days.last.year' />
    </th>
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

<tr class="odd">
    <th>
        <spring:message code='overview.valid.period' />
    </th>
    <td>
        <c:choose>
            <c:when test="${account != null}">
                <joda:format style="M-" value="${account.validFrom}"/> <spring:message code='to' /> <joda:format style="M-" value="${account.validTo}"/>
            </c:when>
            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 
        </c:choose>   
    </td>
</tr>

<tr class="even">
    <th>
        <spring:message code='overview.actual.entitlement' />
    </th>
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

<tr class="odd">
    <th style="border-top: 2px solid #D7D7D7">
        <spring:message code="overview.left" />
    </th>
    <td style="border-top: 2px solid #D7D7D7">
        <c:choose>

            <c:when test="${account != null}">
                <c:set var ="left" value="${leftDays - remLeftDays}" />
                <c:choose>
                    <c:when test="${left <= 1.00 && left > 0.50}">
                        <c:set var="numberOfDays" value="day" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days" />
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${left}" />
                <c:if test="${beforeApril || !account.remainingVacationDaysExpire}">
                +
                <c:set var="remLeftDays" value="${remLeftDays}" />
                <c:choose>
                    <c:when test="${remLeftDays <= 1.00 && remLeftDays > 0.50}">
                        <c:set var="numberOfDays" value="day"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="numberOfDays" value="days"/>
                    </c:otherwise>
                </c:choose>
                <spring:message code="${numberOfDays}" arguments="${remLeftDays}" />&nbsp;<spring:message code="remaining" />
                </c:if>
            </c:when>

            <c:otherwise>
                <spring:message code='not.specified' />
            </c:otherwise> 

        </c:choose> 
    </td>
</tr>