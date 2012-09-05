<%-- 
    Document   : overview_header_user
    Created on : 05.09.2012, 14:36:05
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<table class="overview-header">
    <tr>
        <td><spring:message code="table.overview" /><c:out value="${displayYear}" /></td>
        <td style="text-align: right;">
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
