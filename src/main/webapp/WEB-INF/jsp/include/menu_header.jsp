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


<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container_12">
            <div class="grid_12">
                <a class="brand" href="${formUrlPrefix}/overview">Urlaubsverwaltung</a>
                <div class="nav-collapse collapse">
                    <ul class="nav">

                        <sec:authorize access="hasRole('role.user')">
                            <li><a href="${formUrlPrefix}/overview"><spring:message code="overview" /></a></li>
                        </sec:authorize>

                        <sec:authorize access="hasRole('role.boss')">
                            <li><a href="${formUrlPrefix}/overview"><spring:message code="personal" /></a></li>
                            <li><a href="${formUrlPrefix}/application"><spring:message code="h.apps" /></a></li>
                        </sec:authorize>

                        <sec:authorize access="hasRole('role.office')">
                            <li><a href="${formUrlPrefix}/overview"><spring:message code="personal" /></a></li>
                            <li><a href="${formUrlPrefix}/application"><spring:message code="h.apps" /></a></li>
                            <li><a href="${formUrlPrefix}/staff"><spring:message code="staff.manager" /></a></li>
                        </sec:authorize>

                        <li><a href="<spring:url value='/j_spring_security_logout' />">Logout</a></li>

                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>




