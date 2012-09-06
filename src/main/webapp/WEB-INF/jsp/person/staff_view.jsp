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
                    document.getElementById("staff-active").removeAttribute("selected");
                    document.getElementById("staff-inactive").setAttribute("selected", "selected");
                } else {
                    document.getElementById("staff-inactive").removeAttribute("selected");
                    document.getElementById("staff-active").setAttribute("selected", "selected");
                }
            });
        </script>
        <script type="text/javascript">
            $(document).ready(function() {
                var url = window.location.href;
                var currentYearValue = "?year=" + <c:out value='${year}' />;
                
                var options = document.getElementById("year-selector").options;
                
                for(var i = 0; i < options.length; i++) {
                    
                    var optionValue = options[i].value;
                    
                    if(url.indexOf("?year=") != -1) {
                        if(url.indexOf(optionValue) != -1) {
                            options[i].setAttribute("selected", "selected");
                        } else {
                            options[i].removeAttribute("selected");
                        }
                    } else {
                        if(optionValue == currentYearValue) {
                            options[i].setAttribute("selected", "selected");
                        }
                    }
                    
                }
            
            });
        </script>
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
                            <td>
                                    <spring:message code="table.overview" /><c:out value="${displayYear}" />
                            </td>
                            <td style="text-align: right;">
                                <select id="year-selector" onchange="window.location.href=this.options[this.selectedIndex].value">
                                    <option value="?year=<c:out value='${year - 1}' />"><c:out value="${year - 1}" /></option>
                                    <option selected="selected" value="?year=<c:out value='${year}' />"><c:out value="${year}" /></option>
                                    <option value="?year=<c:out value='${year + 1}' />"><c:out value="${year + 1}" /></option>
                                </select>
                                <select onchange="window.location.href=this.options[this.selectedIndex].value">
                                    <option id="staff-active" value="${formUrlPrefix}/staff"><spring:message code="table.active" /></option>
                                    <option id="staff-inactive" value="${formUrlPrefix}/staff/inactive"><spring:message code="table.inactive" /></option>
                                </select> 
                            </td>
                        </tr>
                    </table>

                    <c:choose>

                        <c:when test="${notexistent == true}">

                            <spring:message code="table.empty" />

                        </c:when>

                        <c:otherwise>
                            <%@include file="./include/staff_list.jsp" %>
                        </c:otherwise>

                    </c:choose>

                </div>
            </div> 
        </div>        

    </body>

</html>
