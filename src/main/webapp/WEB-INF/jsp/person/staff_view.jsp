<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html>

    <spring:url var="URL_PREFIX" value="/web" />

    <head>
        <uv:head />
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
                                    <c:choose>
                                        <c:when test="${param.active}">
                                            <spring:message code="persons.active" /><span class="caret"></span>
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message code="persons.inactive" /><span class="caret"></span>
                                        </c:otherwise>
                                    </c:choose>
                                </a>
                                <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                                    <li>
                                        <a href="${URL_PREFIX}/staff?active=true&year=${year}">
                                            <i class="fa fa-toggle-on"></i>
                                            <spring:message code="persons.active" />
                                        </a>
                                    </li>
                                    <li>
                                        <a href="${URL_PREFIX}/staff?active=false&year=${year}">
                                            <i class="fa fa-toggle-off"></i>
                                            <spring:message code="persons.inactive" />
                                        </a>
                                    </li>
                                </ul>
                            </div>

                            <uv:year-selector year="${year}" hrefPrefix="${URL_PREFIX}/staff?active=${param.active}&year=" />

                            <uv:print />

                            <sec:authorize access="hasRole('OFFICE')">
                                <a href="${URL_PREFIX}/staff/new" class="fa-action pull-right"
                                   data-title="<spring:message code="action.staff.create"/>">
                                    <i class="fa fa-fw fa-user-plus"></i>
                                </a>
                            </sec:authorize>
                            
                        </legend>

                    </div>
                    
                    <c:choose>

                        <c:when test="${empty persons}">
                            <spring:message code="persons.none"/>
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
