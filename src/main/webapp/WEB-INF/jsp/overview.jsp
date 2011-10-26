<%-- 
    Document   : overview
    Created on : 26.10.2011, 11:53:47
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
        
        <table>
            <tr>
                <th colspan="2">
                    <spring:message code="overview.title" />
                </th>
            </tr>
            <tr>
                <th>
                    <spring:message code="overview.person" />
                </th>
                <td>
                    <c:out value="${person.lastname}"/>&nbsp;<c:out value="${person.firstname}"/>
                </td>
            </tr>
            <tr>
                <th>
                    <spring:message code="overview.anspruch" />
                </th>
                <td>
                    <c:out value="${person.vacationDays}"/>&nbsp;<spring:message code="overview.peryear" />
                </td>
            </tr>
            <tr>
                <th>
                    <spring:message code="overview.resturlaub" />&nbsp;<c:out value="${year-1}"/>
                </th>
                <td>
                    <c:out value="${person.restUrlaub}"/>&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <th>
                    <c:out value="${year}"/>&nbsp;<spring:message code="overview.used" />
                </th>
                <td>
                    <c:out value="${person.usedVacationDays}"/>&nbsp;+&nbsp;<c:out value="${person.usedRestUrlaub}"/>&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <th>
                    <c:out value="${year}"/>&nbsp;<spring:message code="overview.uebrig" />
                </th>
                <td>
                    <c:out value="${person.remainingVacationDays}"/>&nbsp;+&nbsp;<c:out value="${person.restUrlaub}"/>
                </td>
            </tr>
        </table>
                
        <br /> 
        <br />
        
        
        <table>
            <tr>
                <th colspan="7">
                    <spring:message code="overview.vac.title" />
                </th>
            </tr>
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
                    <spring:message code="overview.vac.days.brutto" />
                </th>
                <th>
                    <spring:message code="overview.vac.ill" />
                </th>
                <th>
                    <spring:message code="overview.vac.days.netto" />
                </th>
                <th>
                    <spring:message code="overview.vac.state" />
                </th>
                <th>
                    <spring:message code="overview.vac.edit" />
                </th>
            </tr>
            
            <c:set var="${illDays}" value="0"/>
            <c:set var="${vacDays}" value="0"/>
            
            <c:forEach items="${requests}" var="antrag" varStatus="loopStatus">
                <tr>
                    <td>
                        <c:out value="${antrag.vacationType}"/>
                    </td>
                    <td>
                        <c:choose>
                        <c:when test="${antrag.startDate == endDate}">
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
                        <c:out value="${antrag.beantragteTageBrutto}"/>
                    </td>
                    <td>
                        <c:set var="${illDays}" value="${illDays + antrag.krankheitsTage}" />
                        <c:out value="${antrag.krankheitsTage}"/>
                    </td>  
                    <td>
                        <c:set var="${vacDays}" value="${vacDays + (antrag.beantragteTageNetto-antrag.krankheitsTage)}" />
                        <c:out value="${antrag.beantragteTageNetto - antrag.krankheitsTage}"/>
                    </td>                      
                    <td>
                        <c:out value="${antrag.state}" />
                    </td>
                    <td>
                        <!-- Edit-Button -->
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <th>Summe</th>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td><c:out value="${illDays}" /></td>
                <td><c:out value="${vacDays}" /></td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </tr>    
        </table>
        
    </body>
    
</html>
