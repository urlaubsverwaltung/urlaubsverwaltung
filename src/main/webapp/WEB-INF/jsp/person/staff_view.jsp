<%-- 
    Document   : staff_view
    Created on : 17.01.2012, 11:09:37
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
        <script type="text/javascript">
            $(document).ready(function() {
                var path = window.location.pathname;
            
                if(path.indexOf("inactive") != -1) {
                    document.getElementById("staff-inact").setAttribute("class", "a-act");
                    document.getElementById("staff-act").setAttribute("class", "a-inact");
                } else {
                    document.getElementById("staff-inact").setAttribute("class", "a-inact");
                    document.getElementById("staff-act").setAttribute("class", "a-act");
                }
            });
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/menu_header.jsp" %>

        <div id="show-navi">
            <a href="${formUrlPrefix}/staff" id="staff-act"><spring:message code="table.active" /></a>
            <a href="${formUrlPrefix}/staff/inactive" id="staff-inact"><spring:message code="table.inactive" /></a>
        </div>

        <div id="content">
            <div class="container_12">

                <div class="grid_12">
                
                <c:choose>

                    <c:when test="${notexistent == true}">

                        <spring:message code="table.empty" />

                    </c:when>

                    <c:otherwise>

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
                                <td>
                                    <spring:message code="table.overview" /><c:out value="${displayYear}" />
                                </td>
                                <td style="text-align: right;">
                                    <select onchange="window.location.href=this.options[this.selectedIndex].value">
                                        <option selected="selected" value=""><spring:message code="ov.header.year" /></option>
                                        <option value="?year=<c:out value='${year - 1}' />"><c:out value="${year - 1}" /></option>
                                        <option value="?year=<c:out value='${year}' />"><c:out value="${year}" /></option>
                                        <option value="?year=<c:out value='${year + 1}' />"><c:out value="${year + 1}" /></option>
                                    </select> 
                                </td>
                            </tr>
                        </table>
                        
                        <%@include file="./include/staff_list.jsp" %>

                    </c:otherwise>    

                </c:choose>

                </div>
            </div> 
        </div>        

    </body>

</html>
