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
        <title><spring:message code="title" /></title>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <table>
            
            <tr>
                <th>
                    <spring:message code="name" />
                </th>
                <th>
                    <spring:message code="overview.vac.time" />
                </th>
                <th>
                    <spring:message code="overview.vac.days.netto" />
                </th>
                <th>
                    &nbsp;
                </th>
            </tr>
            
            <c:forEach items="${requests}" var="antrag" varStatus="loopStatus">
                <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                    <td><c:out value="${antrag.person.lastName}"/>&nbsp;<c:out value="${antrag.person.firstName}"/></td>
                    <td>
                        <c:choose>
                            <c:when test="${antrag.startDate == antrag.endDate}">
                                am&nbsp;<c:out value="${antrag.startDate}"/>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${antrag.startDate}"/>&nbsp;-&nbsp;<c:out value="${antrag.endDate}"/>
                            </c:otherwise>    
                        </c:choose>
                    </td>
                    <td>
                        <c:out value="${antrag.beantragteTageNetto}" />
                    </td>
                    <td>
                        <a href="${formUrlPrefix}/antrag/${antrag.id}/edit" />
                    </td>
                </tr>    
            </c:forEach>
            
        </table>
    
    </body>
    
</html>
