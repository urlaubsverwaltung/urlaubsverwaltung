<%-- 
    Document   : antraege
    Created on : 31.10.2011, 12:35:58
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
        
        <!-- Personenkärtchen -->
        <table>
            <tr>
                <td rowspan="3">
                    <!--  Bild des Mitarbeiters  -->
                </td>
                <td>
                   <spring:message code="name" />: 
                </td>
                <td>
                   <c:out value="${person.lastName}"/>&nbsp;<c:out value="${person.firstName}"/> 
                </td>
            </tr>
            <tr>
                <td>
                   <spring:message code="table.vac" />
                </td>
                <td>
                    <c:out value="${person.remainingVacationDays}"/>
                </td>
            </tr>
            <tr>
                <td>
                    <spring:message code="table.resturlaub" />
                </td>
                <td>
                    <c:out value="${person.restUrlaub}"/>
                </td>
            </tr>    
            <tr>
                <td colspan="2">
                    <a href="${formUrlPrefix}/antraege/${person.id}"><spring:message code="table.antrag" /></a>
                </td>
            </tr>    
        </table>    
                
        <!-- Antraege der Person als Liste -->
        <table>
            <tr>
                 <th>
                    <spring:message code="overview.vac.type" />
                </th>
                <th>
                    <spring:message code="overview.vac.time" />
                </th>
                <th>
                    <spring:message code="overview.vac.reason" />
                </th>
                <th>
                    <spring:message code="overview.vac.days.netto" />
                </th>
                <th>
                    <spring:message code="overview.vac.state" />
                </th>
                <th>
                    <spring:message code="edit" />
                </th>
            </tr>
            <c:forEach items="${requests}" var="antrag" varStatus="loopStatus">
                <tr class="${loopStatus.index % 2 == 0 ? 'even' : 'odd'}">
                    <td>
                        <c:out value="${antrag.vacationType}"/>
                    </td>
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
                        <c:out value="${antrag.reason}"/>
                    </td> 
                    <td>
                        <c:out value="${antrag.beantragteTageNetto}" />
                    </td>                      
                    <td>
                        <c:out value="${antrag.state}" />
                    </td>
                    <td>
                        <!-- Edit-Button -->
                    </td>
                </tr>
            </c:forEach>
        </table>
        
    </body>
    
</html>
