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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/fluid_grid.css' />" />
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />
        <c:set var="linkPrefix" value="${formUrlPrefix}/application" />

        <%@include file="../include/header.jsp" %>

        <div id="content">

            <div class="container_12">
                
                <div class="grid_12">
                
                <table class="overview-header">
                        <tr>
                            <td><spring:message code="${titleApp}" /></td>
                            <td style="text-align: right;">
                                    <select onchange="window.location.href=this.options
                                        [this.selectedIndex].value">
                                    <option selected="selected" value=""><spring:message code="status.app" /></option>
                                    <option value="${linkPrefix}/waiting"><spring:message code="waiting.app" /></option>
                                    <option value="${linkPrefix}/allowed"><spring:message code="allow.app" /></option>
                                    <option value="${linkPrefix}/cancelled"><spring:message code="cancel.app" /></option>
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


