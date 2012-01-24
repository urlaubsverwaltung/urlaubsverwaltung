<%-- 
    Document   : person_form
    Created on : 31.10.2011, 10:00:10
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

        <%@include file="../include/header.jsp" %>

        <spring:url var="formUrlPrefix" value="/web" />


        <div id="content">

            <form:form method="put" action="${formUrlPrefix}/staff/${person.id}/edit" modelAttribute="personForm"> 
                <table id="person-form-tbl">
                    <tr>
                        <th><spring:message code='person.data' /></th>
                        <th>&nbsp;</th>
                    </tr>
                    <tr>
                        <td><label for="nachname"><spring:message code="lastname" />:</label></td>
                        <td>
                            <form:input id="nachname" path="lastName" cssErrorClass="error" />
                            <form:errors path="lastName" cssClass="error" />
                        </td>
                    </tr>
                    <tr>
                        <td><label for="vorname"><spring:message code="firstname" />:</label></td>
                        <td>
                            <form:input id="vorname" path="firstName" cssErrorClass="error" />
                            <form:errors path="firstName" cssClass="error" />
                        </td>
                    </tr>
                    <tr>
                        <td><label for="email"><spring:message code="email" />:</label></td>
                        <td>
                            <form:input id="email" path="email" cssErrorClass="error" />
                            <form:errors path="email" cssClass="error" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <th>
                            <spring:message code='person.entitlement' />
                        </th>
                        <td>
                            <form:input id="jahr" path="year" cssErrorClass="error" />
                            <form:errors path="year" cssClass="error" />
                        </td>
                    </tr>
                    <tr>
                        <td><label for="urlaubsanspruch"><spring:message code="vac.year" />:</label></td>
                        <td>
                            <form:input id="urlaubsanspruch" path="vacationDays" cssErrorClass="error" />
                            <form:errors path="vacationDays" cssClass="error" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="resturlaub"><spring:message code="remaining" />:</label>
                        </td>
                        <td>
                            <form:input id="resturlaub" path="remainingVacationDays" cssErrorClass="error" />
                            <form:errors path="remainingVacationDays" cssClass="error" /> 
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <input type="submit" name="<spring:message code="save" />" value="<spring:message code="save" />" />
                            <a class="button" href="${formUrlPrefix}/staff"><spring:message code='cancel' /></a>
                            <input type="button" onclick="$('#activ-action').show();"
                                   <c:choose>
                                       <c:when test="${person.active == true}">
                                           name="<spring:message code='person.deactivate' />"
                                           value="<spring:message code='person.deactivate' />"
                                       </c:when>
                                       <c:otherwise>
                                           name="<spring:message code='person.activate' />"
                                           value="<spring:message code='person.activate' />"
                                       </c:otherwise>
                                   </c:choose>    
                                   />
                        </td>
                    </tr>
                </table>

            </form:form>

            <br />
            <br />

            <c:choose>
                <c:when test="${person.active == true}">
                    <c:set var="formUrl" value="${formUrlPrefix}/staff/${person.id}/deactivate" />
                </c:when>
                <c:otherwise>
                    <c:set var="formUrl" value="${formUrlPrefix}/staff/${person.id}/activate" />  
                </c:otherwise>
            </c:choose>

            <form:form method="put" action="${formUrl}">                  
                <div id="activ-action"style="display: none;">
                    <c:choose>
                        <c:when test="${person.active == true}">
                            <spring:message code='person.deactivate.confirm' />&nbsp;
                        </c:when>
                        <c:otherwise>
                            <spring:message code='person.activate.confirm' />&nbsp;
                        </c:otherwise> 
                    </c:choose>       
                    <input type="submit" name="<spring:message code="yes" />" value="<spring:message code="yes" />" />
                    <input type="button" onclick="$('#activ-action').hide();" name="<spring:message code="no" />" value="<spring:message code="no" />" /> 
                </div>         
            </form:form>

        </div>    

    </body>

</html>
