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
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />
        <script type="text/javascript">
            $(document).ready(function() {

                $(".sortable-tbl").tablesorter({
                    sortList: [[1,1]],
                    headers: {
                        1: { sorter: 'germanDate' },
                        4: { sorter: 'germanDate' },
                        5: { sorter: 'commaNumber' }
                    }
                });

            });
        </script>
    </head>

    <body>

        <spring:url var="formUrlPrefix" value="/web" />
        <c:set var="linkPrefix" value="${formUrlPrefix}/application" />

        <uv:menu />

        <div id="content">

            <div class="container_12">

                <div class="grid_12">

                    <%@include file="./include/app-list-elements/list-header.jsp" %> 

                    <%@include file="./include/app-list-elements/list.jsp" %> 

                </div>
            </div>
        </div>            

    </body>

</html>


