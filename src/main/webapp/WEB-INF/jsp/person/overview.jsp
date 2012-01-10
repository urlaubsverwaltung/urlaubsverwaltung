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
            
            <div id="year-navi">
                <a href="${formUrlPrefix}/overview?year=2011">2011</a>
                <a href="${formUrlPrefix}/overview?year=2012">2012</a>
                <a href="${formUrlPrefix}/overview?year=2013">2013</a>
            </div>

            <table id="person-tbl" cellspacing="0" border="1">
                <tr>
                    <td rowspan="6"><img class="user-pic" src="<c:out value='${gravatar}'/>" /></td>
                </tr>
                <tr>
                    <th>
                        <spring:message code="entitlement" />&nbsp;<spring:message code="in.year" />&nbsp;<c:out value="${year}"/>
                    </th>
                    <td>
                        <c:out value="${entitlement.vacationDays + entitlement.remainingVacationDays}"/>
                        <spring:message code="days" />&nbsp;(<spring:message code="davon" />
                        <c:choose>
                            <c:when test="${entitlement.remainingVacationDays == null}">
                                0
                            </c:when>
                            <c:otherwise>
                                <c:out value="${entitlement.remainingVacationDays}"/>
                            </c:otherwise>
                        </c:choose>
                        <spring:message code="days" />&nbsp;<spring:message code="remaining" />)
                    </td>
                </tr>
                <tr>
                    <th>
                        <spring:message code="overview.used" />
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${account != null}">
                                <c:out value="${(entitlement.vacationDays - account.vacationDays) + (entitlement.remainingVacationDays - account.remainingVacationDays)}"/>&nbsp;<spring:message code="days" />
                            </c:when>
                            <c:otherwise>
                                0 <spring:message code="days" />
                            </c:otherwise>
                        </c:choose>    
                    </td>
                </tr>
                <tr>
                    <th>
                        <spring:message code="overview.left" />
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="${account != null}">
                                <c:choose>
                                    <c:when test="${april == 1}">
                                        <c:out value="${account.vacationDays + account.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${account.vacationDays}"/>&nbsp;<spring:message code="days" />
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${entitlement.vacationDays + entitlement.remainingVacationDays}"/>&nbsp;<spring:message code="days" />
                            </c:otherwise>
                        </c:choose>
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
                        <spring:message code="delete" />
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
                            <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                        </td>
                    </tr>
                </c:forEach>
            </table>


        </div>         

    </body>

</html>
