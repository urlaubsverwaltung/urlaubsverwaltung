<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>


<!DOCTYPE html>
<html lang="${language}">

<spring:url var="URL_PREFIX" value="/web"/>

<head>
    <title>
        <spring:message code="person.overview.header.title"/>
    </title>
    <uv:custom-head/>
    <c:if test="${not empty persons}">
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='npm.list.js.js' />"></script>
    <script defer src="<asset:url value='person_view.js' />"></script>
    </c:if>
</head>

<body>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape"/></h4>
</div>

<div class="content print--only-landscape">
    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <legend>

                    <div class="legend-dropdown dropdown">
                        <a id="active-state" href="#" data-toggle="dropdown"
                           aria-haspopup="true" role="button" aria-expanded="false">
                            <c:choose>
                                <c:when test="${param.active}">
                                    <c:choose>
                                        <c:when test="${department != null}">
                                            <c:out value="${department.name}"/><span class="caret"></span>
                                        </c:when>
                                        <c:otherwise>
                                            <spring:message code="persons.active"/><span class="caret"></span>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="persons.inactive"/><span class="caret"></span>
                                </c:otherwise>
                            </c:choose>
                        </a>
                        <ul class="dropdown-menu" role="menu" aria-labelledby="active-state">
                            <li>
                                <a href="${URL_PREFIX}/person?active=true&year=${year}">
                                    <i class="fa fa-fw fa-toggle-on" aria-hidden="true"></i>
                                    <spring:message code="persons.active"/>
                                </a>
                            </li>
                            <c:if test="${departments.size() > 1}">
                                <c:forEach items="${departments}" var="department">
                                    <li>
                                        <a href='${URL_PREFIX}/person?active=true&year=${year}&department=${department.id}'>
                                            <i class="fa fa-fw" aria-hidden="true"></i>
                                            <c:out value="${department.name}"/>
                                        </a>
                                    </li>
                                </c:forEach>
                                <li role="separator" class="divider"></li>
                            </c:if>
                            <li>
                                <a href="${URL_PREFIX}/person?active=false&year=${year}">
                                    <i class="fa fa-fw fa-toggle-off" aria-hidden="true"></i>
                                    <spring:message code="persons.inactive"/>
                                </a>
                            </li>
                        </ul>
                    </div>

                    <uv:year-selector year="${year}"
                                      hrefPrefix="${URL_PREFIX}/person?active=${param.active}&department=${department.id}&year="/>

                    <uv:print/>

                    <c:if test="${userCanBeManipulated}">
                        <sec:authorize access="hasAuthority('OFFICE')">
                            <a href="${URL_PREFIX}/person/new" class="fa-action pull-right" aria-hidden="true"
                               data-title="<spring:message code="action.person.create"/>">
                                <i class="fa fa-fw fa-user-plus" aria-hidden="true"></i>
                            </a>
                        </sec:authorize>
                    </c:if>
                </legend>

                <c:choose>

                    <c:when test="${empty persons}">
                        <spring:message code="persons.none"/>
                    </c:when>

                    <c:otherwise>
                        <%@include file="include/person_list.jsp" %>
                    </c:otherwise>

                </c:choose>

            </div>
        </div>
    </div>
</div>

</body>

</html>
