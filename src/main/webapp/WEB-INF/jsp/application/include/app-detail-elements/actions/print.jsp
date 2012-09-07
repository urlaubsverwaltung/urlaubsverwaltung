<%-- 
    Document   : print
    Created on : 06.09.2012, 13:44:52
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<input type="button" class="btn btn-primary" media="print" onclick="window.print(); return false;" 
       name="<spring:message code='app' />&nbsp;<spring:message code='print' />" 
       value="<spring:message code='app' />&nbsp;<spring:message code='print' />" />
