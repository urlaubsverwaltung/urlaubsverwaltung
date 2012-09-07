<%-- 
    Document   : actions_office
    Created on : 06.09.2012, 11:57:05
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<%-- Office is able to:
* print the application for leave in every state
* button back to the staff member of this application for leave
* cancel the application for leave for another person
--%>

<%-- Print the application for leave --%>
<%@include file="./actions/print.jsp" %>

<%-- Button to staff member --%>
<a class="btn btn-primary" href="${formUrlPrefix}/staff/${application.person.id}/overview" /><spring:message code="staff.back" /></a>

<%-- Cancel the application for leave --%> 
<%@include file="./actions/cancel.jsp" %>