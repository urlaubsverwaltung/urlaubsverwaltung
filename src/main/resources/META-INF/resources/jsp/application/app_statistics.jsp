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
                sortList: [[1, 0]],
                headers: {
                    0: {sorter: false},
                    3: {sorter: false},
                    4: {sorter: false},
                    5: {sorter: 'commaNumber'},
                    6: {sorter: 'commaNumber'},
                    7: {sorter: 'commaNumber'}
                },
                textExtraction: function (node) {
                    var sortable = $(node).find('.sortable');

                    if (sortable.length > 0) {
                        return sortable[0].innerHTML;
                    }

                    return node.innerHTML;
                }
            });

        });
    </script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="linkPrefix" value="${URL_PREFIX}/application"/>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
</div>

<div class="content print--only-landscape">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <legend class="is-sticky">
                    <spring:message code="applications.statistics"/>
                    <uv:print/>
                    <uv:export/>
                </legend>

                <p class="is-inline-block">
                    <c:choose>
                        <c:when test="${not empty errors}">
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="applications.statistics.error.period" />
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="#filterModal" data-toggle="modal">
                                <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
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
                                <th class="hidden-print"><%-- placeholder to ensure correct number of th --%></th>
                                <th class="sortable-field"><spring:message code="person.data.firstName"/></th>
                                <th class="sortable-field"><spring:message code="person.data.lastName"/></th>
                                <th><%-- placeholder to ensure correct number of th --%></th>
                                <th class="hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th class="hidden"><%-- placeholder to ensure correct number of th --%></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.allowed"/></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.waiting"/></th>
                                <th class="sortable-field"><spring:message code="applications.statistics.left"/> (<c:out value="${from.year}" />)</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${statistics}" var="statistic">
                                <tr>
                                    <td class="hidden-print is-centered">
                                        <div class="gravatar img-circle hidden-print" data-gravatar="<c:out value='${statistic.person.gravatarURL}?d=mm&s=60'/>"></div>
                                    </td>
                                    <td class="hidden-xs"><c:out value="${statistic.person.firstName}"/></td>
                                    <td class="hidden-xs"><c:out value="${statistic.person.lastName}"/></td>
                                    <td class="visible-xs hidden-print">
                                        <c:out value="${statistic.person.niceName}"/>
                                    </td>
                                    <td class="visible-xs hidden-print">
                                        <i class="fa fa-fw fa-check"></i>
                                        <uv:number number="${statistic.totalAllowedVacationDays}"/>
                                        <br/>
                                        <i class="fa fa-fw fa-question hidden-print"></i>
                                        <uv:number number="${statistic.totalWaitingVacationDays}"/>
                                    </td>
                                    <td class="hidden-xs hidden-sm">
                                        <spring:message code="applications.statistics.total"/>:
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small><c:out value="${type.displayName}"/>:</small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <b class="sortable"><uv:number number="${statistic.totalAllowedVacationDays}"/></b>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.allowedVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs hidden-sm number">
                                        <b class="sortable"><uv:number number="${statistic.totalWaitingVacationDays}"/></b>
                                        <spring:message code="duration.days"/>
                                        <c:forEach items="${vacationTypes}" var="type">
                                            <br/>
                                            <small>
                                                <uv:number number="${statistic.waitingVacationDays[type]}"/>
                                            </small>
                                        </c:forEach>
                                    </td>
                                    <td class="hidden-xs">
                                        <b class="sortable"><uv:number number="${statistic.leftVacationDays}"/></b>
                                        <spring:message code="duration.vacationDays"/>
                                        <br />
                                        <b><uv:number number="${statistic.leftOvertime}"/></b>
                                        <spring:message code="duration.overtime"/>
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


