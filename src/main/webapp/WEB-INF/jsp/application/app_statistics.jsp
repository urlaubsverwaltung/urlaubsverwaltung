<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<uv:menu/>

<div class="content">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <div class="header">
                    <legend class="is-sticky">
                        <p><spring:message code="applications.statistics"/></p>
                        <uv:print/>
                    </legend>
                </div>

                <p class="is-inline-block">
                    <a href="#filterModal" data-toggle="modal">
                        <spring:message code="time"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                    </a>
                </p>

                <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics"/>

                <c:choose>
                    <c:when test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code='applications.statistics.error'/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table cellspacing="0" class="list-table">
                            <thead class="hidden-xs hidden-sm">
                            <tr>
                                <th><%-- placeholder to ensure correct number of th --%></th>
                                <th><spring:message code="firstname"/></th>
                                <th><spring:message code="lastname"/></th>
                                <th class="is-centered"><spring:message code="applications.statistics.allowed"/></th>
                                <th class="is-centered"><spring:message code="applications.statistics.waiting"/></th>
                                <th class="is-centered"><spring:message code="applications.statistics.left"/></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${persons}" var="person">
                                <tr>
                                    <td class="is-centered">
                                        <img class="img-circle hidden-print"
                                             src="<c:out value='${gravatarUrls[person]}?d=mm&s=60'/>"/>
                                    </td>
                                    <td><c:out value="${person.firstName}"/></td>
                                    <td><c:out value="${person.lastName}"/></td>
                                    <td class="is-centered">
                                        <uv:number number="${allowedDays[person]}"/>
                                    </td>
                                    <td class="is-centered">
                                        <uv:number number="${waitingDays[person]}"/>
                                    </td>
                                    <td class="is-centered">
                                        <uv:number number="${leftDays[person]}"/>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>
        <%-- end of row --%>

    </div>
    <%-- end of container --%>

</div>
<%-- end of content --%>

</body>

</html>


