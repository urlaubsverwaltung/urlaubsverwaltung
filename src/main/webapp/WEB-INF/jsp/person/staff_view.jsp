<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html>

    <head>
        <uv:head />

        <spring:url var="URL_PREFIX" value="/web" />

        <c:choose>
            <c:when test="${!empty param.year}">
                <c:set var="displayYear" value="${param.year}" />
            </c:when>
            <c:otherwise>
                <c:set var="displayYear" value="${year}" />
            </c:otherwise>
        </c:choose>
        
        <script type="text/javascript">
            $(document).ready(function() {

                var path = window.location.pathname;

                var active;
                
                if(path.indexOf("inactive") != -1) {
                    $('#active-state').html('<spring:message code="table.inactive" /><span class="caret"></span>');
                } else {
                    $('#active-state').html('<spring:message code="table.active" /><span class="caret"></span>');
                }

            });
        </script>
    </head>

    <body>

        <uv:menu />

        <div class="content">
            <div class="container">

                <div class="row">

                    <div class="col-xs-12">

                    <div class="header">

                        <legend>

                            <div class="legend-dropdown dropdown">
                                <a id="active-state" data-target="#" href="#" data-toggle="dropdown"
                                   aria-haspopup="true" role="button" aria-expanded="false">
                                    <%-- Filled by javascript --%>
                                </a>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                                    <li>
                                        <a href="${URL_PREFIX}/staff">
                                            <i class="fa fa-toggle-on"></i>
                                            <spring:message code="table.active" />
                                        </a>
                                    </li>
                                    <li>
                                        <a href="${URL_PREFIX}/staff/inactive">
                                            <i class="fa fa-toggle-off"></i>
                                            <spring:message code="table.inactive" />
                                        </a>
                                    </li>
                                </ul>
                            </div>

                            <spring:message code="for" />

                            <uv:year-selector year="${displayYear}" />

                            <uv:print />

                            <a href="${URL_PREFIX}/staff/new" class="fa-action pull-right"
                               data-title="<spring:message code="action.staff.create"/>">
                              <i class="fa fa-fw fa-user-plus"></i>
                            </a>
                            
                        </legend>

                    </div>
                    
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
        </div>        

    </body>

</html>
