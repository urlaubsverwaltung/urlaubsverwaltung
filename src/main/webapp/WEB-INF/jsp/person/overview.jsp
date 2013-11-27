<%-- 
    Document   : overview
    Created on : 26.10.2011, 11:53:47
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>

<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>
    <style type="text/css">
        .app-detail td {
            width: auto;
        }
    </style>
</head>

<body>
<spring:url var="formUrlPrefix" value="/web"/>

<%@include file="../include/menu_header.jsp" %>

<div id="content">

    <div class="container_12">

        <div class="grid_12">

            <c:choose>
                <c:when test="${!empty param.year}">
                    <c:set var="displayYear" value="${param.year}"/>
                </c:when>
                <c:otherwise>
                    <c:set var="displayYear" value="${year}"/>
                </c:otherwise>
            </c:choose>

            <c:choose>
                <c:when test="${person.id == loggedUser.id}">
                    <%@include file="./include/overview_header_user.jsp" %>
                </c:when>
                <c:otherwise>
                    <sec:authorize access="hasAnyRole('role.office', 'role.boss')">
                        <%@include file="./include/overview_header_office.jsp" %>
                    </sec:authorize>
                </c:otherwise>
            </c:choose>

        </div>

        <div class="grid_7 vacation-entitlement">
            <table class="app-detail" cellspacing="0">
                <%@include file="../application/include/account_days.jsp" %>
            </table>
        </div>
        
        <div class="grid_5 vacation-days">
            <table class="app-detail" cellspacing="0">
                <%@include file="./include/used_days.jsp" %>
            </table>
        </div>

        <div class="grid_12">
        
            <div id="datepicker" style="float:right"></div>
            
        </div>

        <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>
        <script>
            $(function() {
                var datepickerLocale = "${pageContext.request.locale.language}";
                var personId = '<c:out value="${person.id}" />';
                var urlPrefix = "<spring:url value='/web/calendar/' />";

                createDatepickerForVacationOverview("#datepicker", datepickerLocale, urlPrefix, personId);
    
            });
        </script>

        <div class="grid_12">&nbsp;</div>
        <div class="grid_12">&nbsp;</div>

        <%@include file="./include/overview_app_list.jsp" %>

        <div class="grid_12 last-element">

            <c:choose>
                <c:when test="${person.id == loggedUser.id}">
                    <%@include file="./include/sick_notes.jsp" %>
                </c:when>
                <c:otherwise>
                    <sec:authorize access="hasRole('role.office')">
                        <%@include file="./include/sick_notes.jsp" %>
                    </sec:authorize>
                </c:otherwise>
            </c:choose>
            
        </div>


    </div>
</div>


</body>


