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

    </head>

    <body>

        <uv:menu />

        <div class="content">
            <div class="container">

                <div class="row">

                    <div class="col-xs-12">

                    <div class="header">

                        <legend>

                           <spring:message code="department.overview.title" />

                           <uv:print />

                            <a href="${URL_PREFIX}/department/new" class="fa-action pull-right"
                               data-title="<spring:message code="action.department.create"/>">
                              <i class="fa fa-fw fa-plus"></i>
                            </a>

                        </legend>

                    </div>

                    <c:choose>

                        <c:when test="${notexistent == true}">

                            <spring:message code="table.empty" />

                        </c:when>

                        <c:otherwise>
                            <%@include file="./department_list.jsp" %>
                        </c:otherwise>

                    </c:choose>

                    </div>
                </div>
            </div>
        </div>

    </body>

</html>
