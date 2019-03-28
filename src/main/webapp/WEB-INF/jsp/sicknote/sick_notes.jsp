<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="assets" uri = "/WEB-INF/assets.tld"%>

<assets:hash var="npm_tablesorter_js" asset="npm.tablesorter.js" />
<assets:hash var="sicknotes_js" asset="sick_notes.js" />


<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        window.uv.sickNote = {};
        window.uv.sickNote.id = "<c:out value="${sickNote.id}" />";
        window.uv.sickNote.person = {};
        window.uv.sickNote.person.id = "<c:out value="${sickNote.person.id}" />";
    </script>
    <script defer type="text/javascript" src="<spring:url value='${npm_tablesorter_js}' />"></script>
    <script defer type="text/javascript" src="<spring:url value='${sicknotes_js}' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape"/></h4>
</div>

<div class="content print--only-landscape">
    <div class="container">

        <div class="row">

            <div class="col-xs-12">

                <legend class="is-sticky">
                    <spring:message code="sicknotes.title"/>
                    <uv:print/>
                    <a href="${URL_PREFIX}/sicknote/statistics" class="fa-action pull-right"
                       data-title="<spring:message code="action.sicknotes.statistics"/>">
                        <i class="fa fa-fw fa-bar-chart" aria-hidden="true"></i>
                    </a>
                    <a href="${URL_PREFIX}/sicknote/new" class="fa-action pull-right"
                       data-title="<spring:message code="action.apply.sicknote"/>">
                        <i class="fa fa-fw fa-plus-circle" aria-hidden="true"></i>
                    </a>
                </legend>

                <uv:filter-modal id="filterModal" actionUrl="${URL_PREFIX}/sicknote/filter"/>

                <p class="is-inline-block">
                    <a href="#filterModal" data-toggle="modal">
                        <spring:message code="filter.period"/>:&nbsp;<uv:date date="${from}"/> - <uv:date date="${to}"/>
                    </a>
                </p>
                <p class="pull-right visible-print">
                    <spring:message code="filter.validity"/> <uv:date date="${today}"/>
                </p>

                <table class="list-table selectable-table sortable tablesorter" cellspacing="0">
                    <thead class="hidden-xs hidden-sm">
                    <tr>
                        <th class="hidden-print"></th>
                        <th class="sortable-field"><spring:message code="person.data.firstName"/></th>
                        <th class="sortable-field"><spring:message code="person.data.lastName"/></th>
                        <th class="hidden"><%-- tablesorter placeholder for first name and last name column in xs screen --%></th>
                        <th class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.title"/></th>
                        <th class="sortable-field"><spring:message
                            code="sicknotes.daysOverview.sickDays.child.title"/></th>
                        <th class="hidden"><%-- tablesorter placeholder for sick days column in xs screen --%></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${persons}" var="person">
                    <tr onclick="navigate('${URL_PREFIX}/staff/${person.id}/overview#anchorSickNotes');">
                        <td class="is-centered hidden-print">
                            <div class="gravatar img-circle hidden-print"
                                 data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
                        </td>
                        <td class="hidden-xs">
                            <c:out value="${person.firstName}"/>
                        </td>
                        <td class="hidden-xs">
                            <c:out value="${person.lastName}"/>
                        </td>
                        <td class="visible-xs">
                            <c:out value="${person.niceName}"/>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-medkit hidden-print" aria-hidden="true"></i>
                            <uv:number number="${sickDays[person].days['TOTAL']}"/>
                            <spring:message code="sicknotes.daysOverview.sickDays.number"/>
                            <c:if test="${sickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message
                                    code="overview.sicknotes.sickdays.aub"
                                    arguments="${sickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-child hidden-print" aria-hidden="true"></i>
                            <uv:number number="${childSickDays[person].days['TOTAL']}"/>
                            <spring:message code="sicknotes.daysOverview.sickDays.child.number"/>
                            <c:if test="${childSickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check positive" aria-hidden="true"></i> <spring:message
                                    code="overview.sicknotes.sickdays.aub"
                                    arguments="${childSickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="visible-xs">
                            <i class="fa fa-medkit hidden-print" aria-hidden="true"></i> <uv:number
                            number="${sickDays[person].days['TOTAL']}"/>
                            <i class="fa fa-child hidden-print" aria-hidden="true"></i> <uv:number
                            number="${childSickDays[person].days['TOTAL']}"/>
                        </td>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

</body>

</html>
