
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<!DOCTYPE html>
<html>

<head>
    <title><spring:message code="title" /></title>
    <%@include file="../include/header.jsp" %>
</head>

<body>

<spring:url var="formUrlPrefix" value="/web" />

<%@include file="../include/menu_header.jsp" %>

<div id="content">
    <div class="container_12">

        <div class="grid_12">

            <div class="overview-header">

                <legend style="margin-bottom: 0">
                    <p>
                        <spring:message code="sicknotes" />&nbsp;<spring:message code="for" />&nbsp;<c:out value="${person.firstName}" />&nbsp;<c:out value="${person.lastName}" />
                    </p>
                    <a class="btn btn-right" href="#" media="print" onclick="window.print(); return false;">
                        <i class="icon-print"></i>&nbsp;<spring:message code='Print' />
                    </a>
                </legend>

            </div>
            
        </div>

        <div class="grid_12">

            <div class="second-legend">
                <p style="float:left">
                    <spring:message code="time"/>:&nbsp;<joda:format style="M-" value="${from}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${to}"/>
                </p>
                <p style="float:right">
                    <spring:message code="Effective"/>&nbsp;<joda:format style="M-" value="${today}"/>
                </p> 
            </div>
            
        </div>

        <div class="grid_12">
            
            <c:choose>

                <c:when test="${empty sickNotes}">
                    <spring:message code="sicknotes.none" />
                </c:when>

                <c:otherwise>
                    <table class="app-tbl centered-tbl sortable-tbl tablesorter zebra-table" cellspacing="0">
                        <thead>
                        <tr>
                            <th><spring:message code="time" /></th>
                            <th><spring:message code="work.days" /></th>
                            <th><spring:message code="sicknotes.aub.short" /></th>
                            <th class="print-invisible"><spring:message code="app.date.overview" /></th>
                            <th class="print-invisible"><spring:message code="table.detail" /></th>
                            <th class="print-invisible"><spring:message code="edit" /></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
                            <tr>
                                <td>
                                    <joda:format style="M-" value="${sickNote.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.endDate}"/>
                                </td>
                                <td>
                                    <fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" />
                                </td>
                                <td>
                                    <c:if test="${sickNote.aubPresent}">
                                        <img src="<spring:url value='/images/black_success.png' />" />
                                    </c:if>
                                    <%--&lt;%&ndash; TODO: other possibility is to show x if not present&ndash;%&gt;--%>
                                    <%--<c:choose>--%>
                                        <%--<c:when test="${sickNote.aubPresent}">--%>
                                            <%--<img src="<spring:url value='/images/black_success.png' />" /> --%>
                                        <%--</c:when>--%>
                                        <%--<c:otherwise>--%>
                                            <%--<img src="<spring:url value='/images/black_fail.png' />" />--%>
                                        <%--</c:otherwise>--%>
                                    <%--</c:choose>--%>
                                </td>
                                <td class="print-invisible">
                                    <joda:format style="M-" value="${sickNote.lastEdited}"/> 
                                </td>
                                <td class="print-invisible">
                                    <a href="${formUrlPrefix}/sicknote/${sickNote.id}">
                                        <img src="<spring:url value='/images/playlist.png' />" />
                                    </a>
                                </td>
                                <td class="print-invisible">
                                    <a href="${formUrlPrefix}/sicknote/${sickNote.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a>
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

</html>
