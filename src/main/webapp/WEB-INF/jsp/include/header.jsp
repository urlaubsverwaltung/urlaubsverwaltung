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

       

        <spring:url var="formUrlPrefix" value="/web/urlaubsverwaltung" />
        
        <div id="top-menu">
            <spring:message code="loggedas" />&nbsp;<c:out value="${user.login}" />     
        </div>
        
        <div id="header">
            
            <h1>Urlaubsverwaltung</h1>

            <div id="main-menu">
                
                <sec:authorize access="hasRole('role.user')">
                <ul>
                    <li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview"><spring:message code="overview" /></a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new"><spring:message code="apply" /></a></li>
                </ul>
                </sec:authorize>
            
                <sec:authorize access="hasRole('role.chef')">
                <ul><li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview"><spring:message code="overview" /></a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new"><spring:message code="apply" /></a></li>
                    <li><a href="${formUrlPrefix}/antraege/wartend"><spring:message code="waiting.app" /></a></li>
                    <li><a href="${formUrlPrefix}/antraege/genehmigt"><spring:message code="allow.app" /></a></li>
                    <li><a href="${formUrlPrefix}/antraege/storniert"><spring:message code="cancel.app" /></a></li>
                    <li><a href="${formUrlPrefix}/mitarbeiter/list"><spring:message code="overview" />&nbsp;<spring:message code="staff" /></a></li>
                </ul>
                </sec:authorize>
            
                <sec:authorize access="hasRole('role.office')">
                <ul><li><a href="${formUrlPrefix}/mitarbeiter/${person.id}/overview"><spring:message code="overview" /></a></li>
                    <li><a href="${formUrlPrefix}/antrag/${person.id}/new"><spring:message code="apply" /></a></li>
                    <li><a href="${formUrlPrefix}/antraege/genehmigt"><spring:message code="allow.app" /></a></li>
                    <li><a href="${formUrlPrefix}/antraege/storniert"><spring:message code="cancel.app" /></a></li>
                    <li><a href="${formUrlPrefix}/mitarbeiter/list"><spring:message code="overview" />&nbsp;<spring:message code="staff" /></a></li>
                    <li><a href="${formUrlPrefix}/manager"><spring:message code="office" /></a></li>
                </ul>
                </sec:authorize>
                
            </div>

        </div>

        
        
