<%-- 
    Document   : list-header
    Created on : 12.04.2012, 10:53:14
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<c:choose>
    <c:when test="${!empty param.year}">
        <c:set var="displayYear" value="${param.year}" />
    </c:when>
    <c:otherwise>
        <c:set var="displayYear" value="${year}" />
    </c:otherwise>
</c:choose>

<table class="overview-header">
    <tr>
        <td><spring:message code="${titleApp}" />&nbsp;&ndash;&nbsp;<c:out value="${displayYear}" /></td>
        <td style="text-align: right;">
            <select onchange="window.location.href=this.options
                                    [this.selectedIndex].value">
                <option selected="selected" value=""><spring:message code="status.app" /></option>
                <option value="${linkPrefix}"><spring:message code="all.app" /></option>
                <option value="${linkPrefix}/waiting"><spring:message code="waiting.app" /></option>
                <option value="${linkPrefix}/allowed"><spring:message code="allow.app" /></option>
                <option value="${linkPrefix}/rejected"><spring:message code="reject.app" /></option>
                <option value="${linkPrefix}/cancelled"><spring:message code="cancel.app" /></option>
            </select>
            &nbsp;
            <select onchange="window.location.href=this.options
                                    [this.selectedIndex].value">
                <option selected="selected" value=""><spring:message code="ov.header.year" /></option>
                <option value="?year=<c:out value='${year - 1}' />"><c:out value="${year - 1}" /></option>
                <option value="?year=<c:out value='${year}' />"><c:out value="${year}" /></option>
                <option value="?year=<c:out value='${year + 1}' />"><c:out value="${year + 1}" /></option>
            </select>
        </td>
    </tr>
</table>
