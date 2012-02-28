<%-- 
    Document   : app_list_boss
    Created on : 13.02.2012, 14:31:35
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<!DOCTYPE html>
<html>

    <head>
        <title><spring:message code="title" /></title>
        <%@include file="../include/header.jsp" %>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />
        <c:set var="linkPrefix" value="${formUrlPrefix}/application" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="content">

            <div class="container_12">

                <div class="grid_12">

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
                                    <option value="${linkPrefix}/waiting"><spring:message code="waiting.app" /></option>
                                    <option value="${linkPrefix}/allowed"><spring:message code="allow.app" /></option>
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

                    <%@include file="./include/list.jsp" %> 

                </div>
            </div>
        </div>            

    </body>

</html>


