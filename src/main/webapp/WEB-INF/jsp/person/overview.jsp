<%-- 
    Document   : overview
    Created on : 26.10.2011, 11:53:47
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="content">

            <table id="person-tbl" cellspacing="0" border="1">
                <tr>
                    <td rowspan="6"><img class="user-pic" src="<spring:url value='/images/steve-jobs.jpg' />" /></td>
                </tr>
                <tr>
                    <th>
                        <spring:message code="entitlement" />
                    </th>
                    <td>
                        <c:out value="${entitlement.vacationDays}"/>&nbsp;<spring:message code="peryear" />
                    </td>
                </tr>
                <tr>
                    <th>
                        <spring:message code="remaining" />&nbsp;<c:out value="${year-1}"/>
                    </th>
                    <td>
                        <c:out value="${entitlement.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                    </td>
                </tr>
                <tr>
                    <th>
                        <c:out value="${year}"/>&nbsp;<spring:message code="overview.used" />
                    </th>
                    <td>
                        <c:out value="${entitlement.vacationDays - account.vacationDays}"/>&nbsp;+&nbsp;<c:out value="${entitlement.remainingVacationDays - account.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                    </td>
                </tr>
                <tr>
                    <th>
                        <c:out value="${year}"/>&nbsp;<spring:message code="days.vac" />
                    </th>
                    <td>
                        <c:out value="${account.vacationDays}"/>
                        <c:if test="${april == 1}">
                            &nbsp;+&nbsp;<c:out value="${account.remainingVacationDays}"/>
                        </c:if>
                    </td>
                </tr>
            </table>

            <br />
            <br />
            <br />
            <br />
            <br />

            <table id="app-tbl" cellspacing="0" border="1">
                <tr>
                    <th>
                        <spring:message code="type" />
                    </th>
                    <th>
                        <spring:message code="time" />
                    </th>
                    <th>
                        <spring:message code="reason" />
                    </th>
                    <th>
                        <spring:message code="days.vac" />
                    </th>
                    <th>
                        <spring:message code="days.ill" />
                    </th>
                    <th>
                        <spring:message code="state" />
                    </th>
                    <th>
                        <spring:message code="edit" />
                    </th>
                </tr>

                <c:forEach items="${applications}" var="app">
                    <tr>
                        <td>
                            <spring:message code="${app.vacationType.vacationTypeName}"/>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${app.startDate == app.endDate}">
                                    am&nbsp;<joda:format style="M-" value="${app.startDate}"/>
                                </c:when>
                                <c:otherwise>
                                    <joda:format style="M-" value="${app.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${app.endDate}"/>
                                </c:otherwise>    
                            </c:choose>
                        </td>
                        <td>
                            <c:out value="${app.reason}"/>
                        </td>
                        <td>
                            <c:out value="${app.days}"/>
                        </td>
                        <td>
                            <c:choose>
                            <c:when test="${app.sickDays == null}">
                                0
                            </c:when>
                            <c:otherwise>
                                 <c:out value="${app.sickDays}"/>
                            </c:otherwise>
                            </c:choose>
                        </td>                     
                        <td>
                            <spring:message code="${app.status.state}" />
                        </td>
                        <td>
                            <a href="${formUrlPrefix}/application/${app.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a>
                        </td>
                    </tr>
                </c:forEach>
            </table>


        </div>         

    </body>

</html>
