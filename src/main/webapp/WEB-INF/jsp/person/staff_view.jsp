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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="<spring:url value='/jquery/js/jquery-1.6.2.min.js' />" type="text/javascript" ></script>
        <script src="<spring:url value='/jquery/js/jquery-ui-1.8.16.custom.min.js' />" type="text/javascript" ></script>
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
        <link rel="stylesheet" type="text/css" href="<spring:url value='/css/main.css' />" /> 
        <title><spring:message code="title" /></title>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />

        <%@include file="../include/header.jsp" %>

        <div id="show-navi">
            <a href="${formUrlPrefix}/staff" id="staff-act"><spring:message code="table.active" /></a>
            <a href="${formUrlPrefix}/staff/inactive" id="staff-inact"><spring:message code="table.inactive" /></a>
        </div>

        <div id="content">

            <c:choose>

                <c:when test="${notexistent == true}">
                    
                    <spring:message code="table.empty" />

                </c:when>

                <c:otherwise>
                    
                    <select size="1" onchange="window.top.location.href=this.options[this.selectedIndex].value">
                        <option selected="selected"><spring:message code="table.choose" /></option>
                        <option value="?view=1"><spring:message code="table.list" /></option>
                        <option value="?view=2"><spring:message code="table.detail" /></option>
                    </select> 

                    <br />
                    <br />

                    <c:if test="${display == 1}">
                        <%@include file="../include/staff_list.jsp" %>
                    </c:if>

                    <c:if test="${display == 2}">
                        <%@include file="../include/staff_detail.jsp" %>
                    </c:if>
                    
                </c:otherwise>    
                    
            </c:choose>

        </div>        

    </body>

</html>
