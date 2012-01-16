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
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" />
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />

        <%@include file="../include/header.jsp" %>

        <div id="content">

            <h2><spring:message code="app.title" /></h2>    

            <table>
                <tr>
                    <td>
                        <spring:message code="name" />:&nbsp;
                    </td>
                    <td>
                        <c:out value="${application.person.lastName}" />&nbsp;<c:out value="${application.person.firstName}" />
                    </td> 
                </tr>
                <tr>
                    <td colspan="2">&nbsp;</td>
                </tr>
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
                        <spring:message code="time" />
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${application.startDate == application.endDate}">
                                am&nbsp;<joda:format style="M-" value="${application.startDate}"/>
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
                    <td>
                        <label for="grund"><spring:message code='reason' />:</label>
                    </td>
                    <td>
                        <c:out value="${application.reason}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="vertreter"><spring:message code='app.rep' />:</label> 
                    </td>
                    <td>
                        <c:out value="${application.rep}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="anschrift"><spring:message code='app.address' />:</label>
                    </td>
                    <td colspan="4">
                        <c:out value="${application.address}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="telefon"><spring:message code='app.phone' />:</label>
                    </td>
                    <td colspan="4">
                        <c:out value="${application.phone}" />
                    </td>
                </tr>
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

                    
            <a class="button" href="${formUrlPrefix}/application/${app.id}/print"><spring:message code='print' /></a>

            <%-- application is waiting --%>            
            <c:if test="${stateNumber == 0}">

                <sec:authorize access="hasRole('role.boss')">         

                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow"> 
                    <input type="submit" name="<spring:message code='app.state.ok' />" value="<spring:message code='app.state.ok' />" class="button" />    
                </form:form>
                <br />    
                    
                <input type="button" name="<spring:message code='app.state.no' />" value="<spring:message code='app.state.no' />" onclick="$('#reject').show(1000);" />
                <br />
                <br /> 
                
                <div id="reject" style="display: none">    
                    <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
                        <spring:message code='reason' />&nbsp;<form:input path="text" />   
                        <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />    
                    </form:form>
                </div>    
                    
                </sec:authorize>
               
            </c:if>

            <%-- application is allowed --%>  
            <c:if test="${stateNumber == 1}">

                <sec:authorize access="hasRole('role.office')">
                    
                    <input type="button" onclick="$('#sick').show();" name="<spring:message code='add.sickdays' />" value="<spring:message code='add.sickdays' />" />
                    <br />
                    <br />
                    <br />
                    
                    <form:form method="put" action="${formUrlPrefix}/application/${application.id}/sick" modelAttribute="appForm">
                        
                    <div id="sick" style="display: none">
                            <spring:message code='staff.sick' />
                            <br />
                            <br />
                            <form:input path="sickDays" />   
                            <input type="submit" name="<spring:message code='save' />" value="<spring:message code='save' />" class="button" />
                    </div>
                    <br />
                    <br />
                    <form:errors path="*" cssClass="error" />
                    </form:form>

                </sec:authorize>
                
            </c:if>

        </div>

    </body>

</html>
