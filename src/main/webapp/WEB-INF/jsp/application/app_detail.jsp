<%-- 
    Document   : app_detail
    Created on : 09.01.2012, 10:12:13
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

        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />

        <%@include file="../include/header.jsp" %>

        <div id="content">
            <div class="container_12">

                <div class="grid_5 app">
                    <h2><spring:message code="app.title" /></h2>    

                    <table id="app-detail">
                        <tr>
                            <td>
                                <spring:message code="app.apply" />
                            </td>
                            <td>
                                <spring:message code="${application.vacationType.vacationTypeName}" />
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td>
                                <spring:message code="time" />:
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.startDate == application.endDate}">
                                        am&nbsp;<joda:format style="M-" value="${application.startDate}"/>,&nbsp;<spring:message code="${application.howLong.dayLength}" />
                                    </c:when>
                                    <c:otherwise>
                                        <joda:format style="M-" value="${application.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${application.endDate}"/>
                                    </c:otherwise>    
                                </c:choose> 
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td><spring:message code="days.vac" />:</td>
                            <td><c:out value="${application.days}"/></td>
                        </tr>
                        <tr>
                            <td><spring:message code="days.ill" />:</td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.sickDays == null}">0</c:when>
                                    <c:otherwise><c:out value="${application.sickDays}"/></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <c:if test="${application.reason != null && !empty application.reason}">
                            <tr>
                                <td>
                                    <label for="grund"><spring:message code='reason' />:</label>
                                </td>
                                <td>
                                    <c:out value="${application.reason}" />
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td>
                                <label for="vertreter"><spring:message code='app.rep' />:</label> 
                            </td>
                            <td>
                                <c:out value="${application.rep}" />
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <c:if test="${application.address!= null && !empty application.address}">
                            <tr>
                                <td>
                                    <label for="anschrift"><spring:message code='app.address' />:</label>
                                </td>
                                <td colspan="4">
                                    <c:out value="${application.address}" />
                                </td>
                            </tr>
                        </c:if>
                        <c:if test="${application.phone != null && !empty application.phone}">
                            <tr>
                                <td>
                                    <label for="telefon"><spring:message code='app.phone' />:</label>
                                </td>
                                <td colspan="4">
                                    <c:out value="${application.phone}" />
                                </td>
                            </tr>
                        </c:if>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td>
                                <spring:message code='app.footer' />&nbsp;<joda:format style="M-" value="${application.applicationDate}"/>
                            </td>
                            <td>&nbsp;</td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                    </table>


                </div> <!-- end of application for leave -->             

                <div class="grid_5 data" style="height: 100%">
                    <table id="tbl-data">    
                        <%@include file="./include/person_data.jsp" %>
                    </table>
                </div>
                    
                <div class="grid_12">&nbsp;</div>
                <div class="grid_12">&nbsp;</div>
                
                <div class="grid_12">
                    <%-- various application's actions dependent on role --%>         
                    <%@include file="./include/app_actions.jsp" %>
                </div>     


            </div> <!-- end of grid container -->

        </div> <!-- end of content -->

    </body>

</html>
