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
                    <sec:authorize access="hasRole('role.office')">
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

        <div class="grid_12">&nbsp;</div>
        <div class="grid_12">&nbsp;</div>

        <div class="grid_12">

            <c:choose>
                <c:when test="${person.id == loggedUser.id}">
                    <a class="btn" href="${formUrlPrefix}/application/new">
                        <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                    </a>
                </c:when>
                <c:otherwise>
                    <sec:authorize access="hasRole('role.office')">
                        <c:if test="${person.id != loggedUser.id}">
                            <a class="btn"
                               href="${formUrlPrefix}/${person.id}/application/new">
                                <c:set var="staff" value="${person.firstName} ${person.lastName}"/>
                                <i class="icon-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                            </a>
                        </c:if>
                    </sec:authorize>
                </c:otherwise>
            </c:choose>

            <a class="btn" href="#" media="print" onclick="window.print(); return false;">
                <i class="icon-print"></i>&nbsp;<spring:message code='overview' />&nbsp;<spring:message code='print' />
            </a>

        </div>
        <div class="grid_12">&nbsp;</div>


        <%@include file="./include/overview_app_list.jsp" %>

        <div class="grid_12 last-element">

            <div class="overview-header">

                <legend id="sickNotes">
                    <p>
                       <spring:message code="sicknotes" />
                    </p>
                </legend>

            </div>

            <c:choose>

                <c:when test="${empty sickNotes}">
                    <spring:message code="sicknotes.none" />
                </c:when>

                <c:otherwise>
                    <table class="app-tbl centered-tbl sortable-tbl tablesorter zebra-table" cellspacing="0">
                        <thead>
                        <tr>
                            <th><spring:message code="sicknotes.time" /></th>
                            <th><spring:message code="work.days" /></th>
                            <th><spring:message code="sicknotes.aub.short" /></th>
                            <th class="print-invisible"><spring:message code="app.date.overview" /></th>
                            <th class="print-invisible"><spring:message code="table.detail" /></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
                        <c:choose>
                            <c:when test="${sickNote.active}">
                                <c:set var="CSS_CLASS" value="active" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="CSS_CLASS" value="inactive" />
                            </c:otherwise>
                        </c:choose>
                        <tr class="${CSS_CLASS}">
                            <td>
                                <joda:format style="M-" value="${sickNote.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.endDate}"/>
                            </td>
                            <td>
                                <fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" />
                            </td>
                            <td>
                                <joda:format style="M-" value="${sickNote.aubStartDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.aubEndDate}"/>
                            </td>
                            <td class="print-invisible">
                                <joda:format style="M-" value="${sickNote.lastEdited}"/>
                            </td>
                            <td class="print-invisible">
                                <a href="${formUrlPrefix}/sicknote/${sickNote.id}">
                                    <img src="<spring:url value='/images/playlist.png' />" />
                                </a>
                            </td>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>

            </c:choose>
            
        </div>


    </div>
</div>


</body>


