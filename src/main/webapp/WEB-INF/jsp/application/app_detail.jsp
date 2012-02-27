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

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="content">
            <div class="container_12">
                 
                <div class="grid_12">&nbsp;</div>

                <div class="grid_5 app">
                    <h2><spring:message code="app.title" /></h2>

                    <table id="app-detail">
                        <tr>
                            <td>
                                <spring:message code="state" />:
                            </td>
                            <td>
                                <spring:message code="${application.status.state}" />
                            </td>
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
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td>
                                <label for="grund"><spring:message code='reason' />:</label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.reason != null && !empty application.reason}">
                                        <c:out value="${application.reason}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
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
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td>
                                <label for="anschrift"><spring:message code='app.address' />:</label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.address!= null && !empty application.address}">
                                        <c:out value="${application.address}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <label for="telefon"><spring:message code='app.phone' />:</label>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${application.phone != null && !empty application.phone}">
                                        <c:out value="${application.phone}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="not.stated" />
                                    </c:otherwise>
                                </c:choose>
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
                    </table>
                        
                            <sec:authorize access="hasRole('role.office')">
                        <a class="button print" href="${formUrlPrefix}/application/${application.id}/print"><spring:message code='app' />&nbsp;<spring:message code='print' /></a>
                            </sec:authorize>
                        
                         <%-- application is waiting --%>            
                    <c:if test="${stateNumber == 0}">

                        <sec:authorize access="hasRole('role.boss')">         

                            <div class="boss-view">
                            <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow"> 
                                <input class="confirm" type="submit" name="<spring:message code='app.state.ok' />" value="<spring:message code='app.state.ok' />" class="button" />    
                                <input class="back" type="button" name="<spring:message code='app.state.no' />" value="<spring:message code='app.state.no' />" onclick="$('#reject').show();" />
                            </form:form>   
                                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
                                 <br />
                            <div id="reject" style="
                                     <c:choose>
                                         <c:when test="${empty errors}">display: none</c:when>
                                         <c:otherwise>display: block</c:otherwise>
                                     </c:choose>
                                ">            
                                    <spring:message code='reason' />:&nbsp;&nbsp;<form:input path="text" cssErrorClass="error" />   
                                    <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />
                                    <script type="text/javascript">
                                    $(document).ready(function() {
                                        $('#reject-error').show('drop', 500);
                                    });
                                </script>
                                    <form:errors path="text" cssClass="error" id="reject-error" />
                                </div>
                                </form:form>  
                            </div>
                        </sec:authorize>

                    </c:if>
                </div> <!-- end of application for leave -->             

                <div class="grid_5 data">
                    <table id="tbl-data">    
                        <tr>
                            <td rowspan="2"><img style="margin-left: 1.5em;"class="user-pic" src="<c:out value='${gravatar}?d=mm'/>" /></td>
                            <td style="font-size:1.1em;"><c:out value="${application.person.firstName}" />&nbsp;<c:out value="${application.person.lastName}" />
                                <br />
                                <br />
                                <c:out value="${application.person.email}" /></td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <tr>
                            <td colspan="2">&nbsp;</td>
                        </tr>
                        <%@include file="./include/account_days.jsp" %>
                    </table>
                    
                        <sec:authorize access="hasRole('role.office')">
                            <a class="button staff" href="${formUrlPrefix}/staff/${application.person.id}/overview" /><spring:message code="staff.back" /></a>
                        </sec:authorize>
                
                        
                </div>
                

                    <%-- if user wants to cancel an application --%>
                    <c:if test="${stateNumber == 4}">
                        <div class="grid_12">&nbsp;</div>
                        <div class="grid_12">&nbsp;</div>
                        <div class="grid_12">
                        <sec:authorize access="hasRole('role.office')">
                        <c:if test="${application.status.number == 0 || app.status.number == 1}">      
                        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                            <spring:message code='cancel.confirm' />&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                            <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                        </form:form>
                        </c:if>
                        </sec:authorize>
                            
                        <sec:authorize access="hasRole('role.user')">
                        <c:if test="${application.status.number == 0}">      
                        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                            <spring:message code='cancel.confirm' />&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                            <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                        </form:form>
                        </c:if>
                        </sec:authorize>  
                            
                        <sec:authorize access="hasRole('role.boss')">
                        <c:if test="${application.status.number == 0}">      
                        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                            <spring:message code='cancel.confirm' />&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                            <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                        </form:form>
                        </c:if>
                        </sec:authorize>  
                            
                        </div>
                    </c:if>



            </div> <!-- end of grid container -->

        </div> <!-- end of content -->

    </body>

</html>
