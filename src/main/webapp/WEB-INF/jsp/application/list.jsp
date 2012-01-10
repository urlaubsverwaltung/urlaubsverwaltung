<%-- 
    Document   : wartend
    Created on : 26.10.2011, 15:03:30
    Author     : aljona
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
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <%@include file="../include/header.jsp" %>

        <div id="content">
        
        <table id="app-tbl" cellspacing="0" border="1">
                <tr>
                    <th>
                        <spring:message code="staff" />
                    </th>
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
                    <c:choose>
                        <%-- if applications are sort by person (stateNumber == 3), the field 'th' doesn't exist --%>
                        <c:when test="${stateNumber == 3}">
                        </c:when>
                        <%-- if applications are cancelled (stateNumber == 2), only printing is possible, not editing --%>
                        <c:when test="${stateNumber == 2}">
                            <th>
                                <spring:message code="print" />
                            </th>
                        </c:when>
                        <%-- if applications are waiting OR allowed (stateNumber == 0 OR 1), you also may edit the application --%>
                        <c:otherwise>
                            <th>
                                <spring:message code="edit" />
                            </th>
                        </c:otherwise>
                    </c:choose>
                </tr>

                <c:forEach items="${applications}" var="app">
                    <tr>
                        <td>
                            <c:out value="${app.person.lastName}" />&nbsp;<c:out value="${app.person.firstName}" />
                        </td>
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
                        
                        <%-- four possible cases: --%>
                        
                        <%-- 0 : applications are waiting --%>
                        <c:if test="${stateNumber == 0}">
                            <td><a href="${formUrlPrefix}/application/${app.id}?state=0"><img src="<spring:url value='/images/edit.png' />" /></a></td>
                        </c:if>
                        
                        <%-- 1 : applications are allowed --%>
                        <c:if test="${stateNumber == 1}">
                            <td><a href="${formUrlPrefix}/application/${app.id}?state=1"><img src="<spring:url value='/images/edit.png' />" /></a></td>
                        </c:if>
                        
                        <%-- 2 : applications are cancelled --%>
                        <c:if test="${stateNumber == 2}">
                            <td><a href="${formUrlPrefix}/application/${app.id}?state=2"><img src="<spring:url value='/images/playlist.png' />" /></a></td>
                        </c:if>
                            
                        <%-- 3 : applications sort by person 
                                 show nothing
                        --%>
    
                    </tr>
                </c:forEach>
            </table>
                    
        </div>            
    
    </body>
    
</html>
