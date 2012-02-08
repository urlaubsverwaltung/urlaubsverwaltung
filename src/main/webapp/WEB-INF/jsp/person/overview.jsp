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
        <script src="<spring:url value='/jquery/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/jquery/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/fluid_grid.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="content">

            <div class="container_12">

                <div class="grid_9"> 

                    <c:choose>
                        <c:when test="${!empty param.year}">
                            <c:set var="displayYear" value="${param.year}" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="displayYear" value="${year}" />
                        </c:otherwise>
                    </c:choose>

                    <table class="overview-header">
                        <tr>
                            <td><spring:message code="table.overview" /><c:out value="${displayYear}" /></td>
                            <td style="text-align: right;">
                                    <select onchange="window.location.href=this.options
                                        [this.selectedIndex].value">
                                    <option selected="selected" value=""><spring:message code="ov.header.year" /></option>
                                    <option value="?year=<c:out value='${year - 1}' />"><c:out value="${year - 1}" /></option>
                                    <option value="?year=<c:out value='${year}' />"><c:out value="${year}" /></option>
                                    <option value="?year=<c:out value='${year + 1}' />"><c:out value="${year + 1}" /></option>
                                </select>
                            </td>
                        </tr>
                    </table>
                </div>

                <div class="grid_9">
                    <table id="person-tbl" cellspacing="0">
                        <c:choose>
                            <c:when test="${!empty param.year}">
                                <c:set var="displayYear" value="${param.year}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="displayYear" value="${year}" />
                            </c:otherwise>
                        </c:choose>

                        <tr>
                            <td rowspan="6" style="background-color: #EAF2D3; width: 13%;"><img class="user-pic" src="<c:out value='${gravatar}?d=mm'/>" /></td>
                        </tr>
                        <%@include file="../application/include/account_days.jsp" %>
                    </table>
                </div>

                <div class="grid_3">&nbsp;</div> 

                <div class="grid_12">&nbsp;</div>
                <div class="grid_12">&nbsp;</div>


                <div class="grid_9">
                    <c:choose>

                        <c:when test="${noapps == true}">
                            <spring:message code='no.apps' />
                        </c:when>

                        <c:otherwise>

                            <table class="app-tbl" cellspacing="0">
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
                                    <th class="td-detail">
                                        <spring:message code="table.detail" />
                                    </th>
                                    <c:if test="${isOffice != true}">
                                        <th style="text-align: center">
                                            <spring:message code="delete" />
                                        </th>
                                    </c:if>
                                </tr>

                                <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                                    <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
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
                                        <td class="td-detail"><a href="${formUrlPrefix}/application/${app.id}"><img src="<spring:url value='/images/playlist.png' />" /></a></td>
                                                <c:if test="${isOffice != true}">
                                            <td style="text-align: center">
                                                <a href="${formUrlPrefix}/application/${app.id}/cancel"><img src="<spring:url value='/images/cancel.png' />" /></a>
                                            </td>
                                        </c:if>
                                    </tr>
                                </c:forEach>
                            </table>

                        </c:otherwise>

                    </c:choose>
                </div>
                <div class="grid_3">&nbsp;</div>

            </div>
        </div>

    </body>

</html>
