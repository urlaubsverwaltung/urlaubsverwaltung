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
                </div>

                <div class="grid_12">
                    <table id="person-tbl" cellspacing="0">
                        <c:choose>
                            <c:when test="${!empty param.year}">
                                <c:set var="displayYear" value="${param.year}" />
                            </c:when>
                            <c:otherwise>
                                <c:set var="displayYear" value="${year}" />
                            </c:otherwise>
                        </c:choose>

                        <tr>
                            <td rowspan="3" style="background-color: #EAF2D3; width: 9.7%;"><img class="user-pic" src="<c:out value='${gravatar}?d=mm'/>" /></td>
                        <%@include file="../application/include/account_days.jsp" %>

                    </table>
                </div>


                <div class="grid_12">&nbsp;</div>
                <div class="grid_12">&nbsp;</div>
                <div class="grid_12">&nbsp;</div>


                <%@include  file="./include/overview_app_list.jsp" %>
                

            </div>
        </div>

    </body>

</html>
