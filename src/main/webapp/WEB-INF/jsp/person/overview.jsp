<%-- 
    Document   : overview
    Created on : 26.10.2011, 11:53:47
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>

    <head>
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
        <style type="text/css">
            .app-detail td {
                width: auto;
            }
        </style>
    </head>

    <body>
        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

    <sec:authorize access="hasAnyRole('role.user', 'role.boss', 'role.office')">
        <%@include file="overview_person.jsp" %>
    </sec:authorize>

    <sec:authorize access="hasRole('role.admin')">
        <%@include file="overview_admin.jsp" %>
    </sec:authorize>
</body>


