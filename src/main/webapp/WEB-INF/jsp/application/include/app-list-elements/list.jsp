<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<c:choose>

    <c:when test="${empty applications}">

        <spring:message code="no.apps"/>

    </c:when>

    <c:otherwise>

        <script type="text/javascript">
            $(document).ready(function() {

                $("table.sortable").tablesorter({
                    sortList: [[2,0]],
                    headers: {
                        2: { sorter: 'germanDate' },
                        3: { sorter: 'commaNumber' }
                    },
                    textExtraction: function(node) {

                        var sortable = $(node).find(".sortable");

                        if(sortable.length > 0) {
                            return sortable[0].innerHTML;
                        }

                        return node.innerHTML;
                    }
                });

            });
        </script>

        <table class="list-table selectable-table sortable tablesorter" cellspacing="0">
            <thead class="hidden-xs hidden-sm">
            <tr>
                <th class="visible-print"><%-- placeholder to ensure correct number of th --%></th>
                <th><%-- placeholder to ensure correct number of th --%></th>
                <th class="sortable-field"><spring:message code="time" /></th>
                <th class="sortable-field"><spring:message code="days.vac" /></th>
                <th class="sortable-field"><spring:message code="staff" /></th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${applications}" var="app" varStatus="loopStatus">
                <c:choose>
                    <c:when test="${app.status.state == 'state.cancelled' || app.status.state == 'state.rejected'}">
                        <c:set var="CSS_CLASS" value="inactive"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="CSS_CLASS" value="active"/>
                    </c:otherwise>
                </c:choose>
                <tr class="${CSS_CLASS}" onclick="navigate('${URL_PREFIX}/application/${app.id}');">
                    <td class="visible-print">
                        <spring:message code="${app.status.state}"/>
                    </td>
                    <td class="is-centered state ${app.status} hidden-print">
                        <c:choose>
                            <c:when test="${app.status == 'WAITING'}">
                                <i class="fa fa-question"></i>
                            </c:when>
                            <c:when test="${app.status == 'ALLOWED'}">
                                <i class="fa fa-check"></i>
                            </c:when>
                            <c:when test="${app.status == 'REJECTED'}">
                                <i class="fa fa-ban"></i>
                            </c:when>
                            <c:when test="${app.status == 'CANCELLED'}">
                                <i class="fa fa-trash"></i>
                            </c:when>
                            <c:otherwise>
                                &nbsp;
                            </c:otherwise>
                        </c:choose>
                    </td>
                    <td>
                        <a class="vacation ${app.vacationType} hidden-print" href="${URL_PREFIX}/application/${app.id}">
                            <h4><spring:message code="${app.vacationType.vacationTypeName}"/></h4>
                        </a>

                        <h4 class="visible-print">
                            <spring:message code="${app.vacationType.vacationTypeName}"/>
                        </h4>

                        <p class="sortable">
                            <c:choose>
                                <c:when test="${app.startDate == app.endDate}">
                                    <uv:date date="${app.startDate}"/>, <spring:message
                                        code="${app.howLong.dayLength}"/>
                                </c:when>
                                <c:otherwise>
                                    <uv:date date="${app.startDate}"/> - <uv:date date="${app.endDate}"/>
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </td>
                    <td class="is-centered hidden-xs">
                        <span>
                            <span class="sortable"><uv:number number="${app.days}" /></span> Tage
                        </span>
                    </td>
                    <td>
                        <img class="img-circle hidden-print" src="<c:out value='${gravatarUrls[app]}?d=mm&s=60'/>"/>&nbsp;
                        <h5 class="is-inline-block hidden-xs hidden-print" style="line-height: 60px; vertical-align: middle">
                            <a class="sortable" href="${URL_PREFIX}/staff/${app.person.id}/overview">
                                <c:out value="${app.person.niceName}"/>
                            </a>
                        </h5>
                        <p class="visible-print is-centered"><c:out value="${app.person.niceName}"/></p>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>  

