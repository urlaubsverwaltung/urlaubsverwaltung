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
    <script type="text/javascript">
        $(document).ready(function() {

            $("table.sortable").tablesorter({
                sortList: [[1,0]],
                headers: {
                    3: { sorter: 'commaNumber' },
                    4: { sorter: 'commaNumber' },
                    5: { sorter: 'commaNumber' }
                }
            });

        });
    </script>
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
                        <spring:message code="applications.statistics"/>
                        <uv:print/>
                    </legend>
                </div>

                <p class="is-inline-block">
                    <c:choose>
                        <c:when test="${not empty errors}">
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="applications.statistics.error.period" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="time"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                            </a>
                        </c:otherwise>
                    </c:choose>
                </p>

                <uv:filter-modal id="filterModal" actionUrl="${linkPrefix}/statistics"/>

                <c:choose>
                    <c:when test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code='applications.statistics.error'/>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <table cellspacing="0" class="list-table sortable tablesorter">
                            <thead class="hidden-xs hidden-sm">
                            <tr>
                                <th><%-- placeholder to ensure correct number of th --%></th>
                                <th class="sortable-field"><spring:message code="person.data.firstName"/></th>
                                <th class="sortable-field"><spring:message code="person.data.lastName"/></th>
                                <th class="sortable-field is-centered"><spring:message code="applications.statistics.allowed"/></th>
                                <th class="sortable-field is-centered"><spring:message code="applications.statistics.waiting"/></th>
                                <th class="sortable-field is-centered"><spring:message code="applications.statistics.left"/> (<c:out value="${from.year}" />)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${statistics}" var="statistic">
                                <tr>
                                    <td class="is-centered">
                                        <img class="img-circle hidden-print"
                                             src="<c:out value='${statistic.gravatarUrl}?d=mm&s=60'/>"/>
                                    </td>
                                    <td><c:out value="${statistic.person.firstName}"/></td>
                                    <td><c:out value="${statistic.person.lastName}"/></td>
                                    <td class="is-centered">
                                        <uv:number number="${statistic.allowedVacationDays}"/>
                                    </td>
                                    <td class="is-centered">
                                        <uv:number number="${statistic.waitingVacationDays}"/>
                                    </td>
                                    <td class="is-centered">
                                        <uv:number number="${statistic.leftVacationDays}"/>
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


