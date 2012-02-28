<%-- 
    Document   : header
    Created on : 19.10.2011, 15:21:35
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

       

        <spring:url var="formUrlPrefix" value="/web" />
        
        <div id="top-menu">
            <spring:message code="loggedas" />&nbsp;<sec:authentication property="principal.username" />&nbsp;&nbsp;  
            <a class="logout" href="<spring:url value='/j_spring_security_logout' />">Logout</a>
        </div>
        
        <div id="header">
            
            <h1><spring:message code="title" /></h1>

            <div 
            <div id="main-menu">
                
                <sec:authorize access="hasRole('role.user')">
                    <ul>
                        <li><a href="${formUrlPrefix}/overview"><spring:message code="overview" /></a></li>
                        <li><a href="${formUrlPrefix}/application/new"><spring:message code="apply" /></a></li>
                    </ul>    
                </sec:authorize>
                
                <sec:authorize access="hasRole('role.boss')">
                    <ul>
                        <li><a href="${formUrlPrefix}/overview"><spring:message code="overview" /></a></li>
                        <li><a href="${formUrlPrefix}/application/new"><spring:message code="apply" /></a></li>
                        <li><a href="${formUrlPrefix}/application"><spring:message code="h.apps" /></a></li>
                    </ul>
                </sec:authorize>
                
                <sec:authorize access="hasRole('role.office')">
                    <ul>
                        <li><a href="${formUrlPrefix}/overview"><spring:message code="overview" /></a></li>
                        <li><a href="${formUrlPrefix}/application/new"><spring:message code="apply" /></a></li>
                        <li><a href="${formUrlPrefix}/application"><spring:message code="h.apps" /></a></li>
                        <li><a href="${formUrlPrefix}/staff"><spring:message code="staff.manager" /></a></li>
                    </ul>
                </sec:authorize>
                
            </div>

        </div>

        
        
