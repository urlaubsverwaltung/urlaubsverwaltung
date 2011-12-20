<%-- 
    Document   : app_form
    Created on : 26.10.2011, 15:05:51
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
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/ui-lightness/jquery-ui-1.8.16.custom.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
        <script src="<spring:url value='/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
        <script type="text/javascript">
	$(function() {
		$( "#datepicker" ).datepicker();
	});
	</script>
    </head>
    
    <body>
        
        <spring:url var="formUrlPrefix" value="/web" />
        
        <%@include file="../include/header.jsp" %>
        
        <div id="content">
        
        <h2><spring:message code="application.title" /></h2>
        
        <br />
        
        <form:form method="post" action="${formUrlPrefix}/${person.id}/application/new" modelAttribute="application"> 
            <form:hidden path="id" />
            
        <table>
            <tr>
                <td>
                    <spring:message code="name" />:&nbsp;
                </td>
                <td>
                    <c:out value="${person.lastName}" />,&nbsp;<c:out value="${person.firstName}" />
                </td> 
            </tr>
            <tr>
                <td>
                    <spring:message code="application.anspruch" />:
                </td>    
                <td>    
                    <c:out value="${account.vacationDays}" />&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <td>
                  <spring:message code="resturlaub" />:  
                </td>
                <td>
                    <c:out value="${account.remainingVacationDays}" />&nbsp;<spring:message code="days" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <spring:message code="application.application" />
                </td>
                <td>
                   <form:select path="vacationType" size="1">
                        <c:forEach items="${vacTypes}" var="vacType">
                                <option value="${vacType}">
                                    <spring:message code='${vacType.vacationTypeName}' />
                                </option>
                        </c:forEach>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>Von:&nbsp;&nbsp;<form:input id="startDate" path="startDate" /></td>
                <td>Bis:&nbsp;&nbsp;<form:input id="endDate" path="endDate" /></td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <form:select path="howLong" size="1">
                         <c:forEach items="${daylength}" var="length">
                                <option value="${length}">
                                    <spring:message code='${length.dayLength}' />
                                </option>
                        </c:forEach>
                    </form:select>
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="reason"><spring:message code='application.reason' />:</label>
                </td>
                <td>
                    <form:input id="reason" path="reason" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                   <label for="vertreter"><spring:message code='application.vertreter' />:</label> 
                </td>
                <td colspan="2">
                    <form:select path="rep" id="vertreter" size="1">
                        <c:forEach items="${persons}" var="einmitarbeiter">
                                <option value="${einmitarbeiter.lastName} ${einmitarbeiter.firstName}">
                                    <c:out value='${einmitarbeiter.lastName}' />&nbsp;<c:out value="${einmitarbeiter.firstName}" />
                                </option>
                        </c:forEach>
                    </form:select>                             
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td>
                    <label for="anschrift"><spring:message code='application.anschrift' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="anschrift" path="address" />
                </td>
            </tr>
            <tr>
                <td>
                    <label for="telefon"><spring:message code='application.phone' />:</label>
                </td>
                <td colspan="4">
                    <form:input id="telefon" path="phone" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <spring:message code='application.ort' />&nbsp;<c:out value="${date}" /><form:hidden path="applicationDate" />
                </td>
            </tr>
            <tr>
                <td colspan="3">&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <input type="submit" name="<spring:message code='apply' />" value="<spring:message code='apply' />" />
                </td>
            </tr>
            
        </table>
                
        </form:form>    
        
        
        </div>
        
    </body>
    
</html>
