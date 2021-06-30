<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="sicknotes.header.title"/>
    </title>
    <uv:custom-head/>
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
    <uv:datepicker-localisation />
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />" />
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='npm.tablesorter.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~95889e93.js' />"></script>
    <script defer src="<asset:url value='sick_notes.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu/>

<div class="content">
    <div class="container">

        <uv:section-heading>
            <jsp:attribute name="actions">
                <a href="${URL_PREFIX}/sicknote/new" class="icon-link tw-px-1" data-title="<spring:message code="action.apply.sicknote"/>">
                    <icon:plus-circle className="tw-w-5 tw-h-5" />
                </a>
                <a href="${URL_PREFIX}/absences" class="icon-link tw-px-1" data-title="<spring:message code="action.applications.absences_overview"/>">
                    <icon:calendar className="tw-w-5 tw-h-5" />
                </a>
                <a href="${URL_PREFIX}/sicknote/statistics" class="icon-link tw-px-1" data-title="<spring:message code="action.sicknotes.statistics"/>">
                    <icon:presentation-chart-bar className="tw-w-5 tw-h-5" />
                </a>
                <uv:print/>
            </jsp:attribute>
            <jsp:attribute name="below">
                <p class="tw-text-sm">
                    <a href="#filterModal" data-toggle="modal">
                        <spring:message code="filter.period"/>:&nbsp;<uv:date-range from="${from}" to="${to}" />
                    </a>
                </p>
                <p class="pull-right visible-print">
                    <spring:message code="filter.validity"/> <uv:date date="${today}"/>
                </p>
                <uv:filter-modal id="filterModal" actionUrl="${URL_PREFIX}/sicknote/filter"/>
            </jsp:attribute>
            <jsp:body>
                <h1>
                    <spring:message code="sicknotes.title"/>
                </h1>
            </jsp:body>
        </uv:section-heading>

        <div class="row">
            <div class="col-xs-12">
                <table class="list-table selectable-table sortable tablesorter tw-text-sm">
                    <thead class="hidden-xs hidden-sm">
                    <tr>
                        <th scope="col" class="print:tw-hidden"></th>
                        <th scope="col" class="sortable-field"><spring:message code="person.data.firstName"/></th>
                        <th scope="col" class="sortable-field"><spring:message code="person.data.lastName"/></th>
                        <th scope="col" class="hidden"><%-- tablesorter placeholder for first name and last name column in xs screen --%></th>
                        <th scope="col" class="sortable-field"><spring:message code="sicknotes.daysOverview.sickDays.title"/></th>
                        <th scope="col" class="sortable-field"><spring:message
                            code="sicknotes.daysOverview.sickDays.child.title"/></th>
                        <th scope="col" class="hidden"><%-- tablesorter placeholder for sick days column in xs screen --%></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${persons}" var="person">
                    <tr onclick="navigate('${URL_PREFIX}/person/${person.id}/overview#anchorSickNotes');">
                        <td class="is-centered print:tw-hidden">
                            <uv:avatar
                                url="${person.gravatarURL}?d=mm&s=40"
                                username="${person.niceName}"
                                width="40px"
                                height="40px"
                                border="true"
                            />
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
                            <div class="tw-flex tw-items-center">
                                <icon:medkit className="tw-w-4 tw-h-4" />
                                &nbsp;<uv:number number="${sickDays[person].days['TOTAL']}"/>
                                <spring:message code="sicknotes.daysOverview.sickDays.number"/>
                            </div>
                            <c:if test="${sickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row tw-flex tw-items-center">
                                    <span class="tw-text-green-500 tw-flex tw-items-center">
                                        <icon:check className="tw-w-4 tw-h-4" />
                                    </span>
                                    &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="hidden-xs">
                            <div class="tw-flex tw-items-center">
                                <icon:child className="tw-w-3 tw-h-3" />
                                &nbsp;<uv:number number="${childSickDays[person].days['TOTAL']}"/>
                                <spring:message code="sicknotes.daysOverview.sickDays.child.number"/>
                            </div>
                            <c:if test="${childSickDays[person].days['WITH_AUB'] > 0}">
                                <p class="list-table--second-row tw-flex tw-items-center">
                                    <span class="tw-text-green-500 tw-flex tw-items-center">
                                        <icon:check className="tw-w-4 tw-h-4" />
                                    </span>
                                    &nbsp;<spring:message code="overview.sicknotes.sickdays.aub" arguments="${childSickDays[person].days['WITH_AUB']}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="visible-xs">
                            <div class="tw-flex tw-items-center">
                                <icon:medkit className="tw-w-3 tw-h-3" />&nbsp;<uv:number number="${sickDays[person].days['TOTAL']}"/>
                                <c:if test="${sickDays[person].days['WITH_AUB'] > 0}">
                                    &nbsp;(&nbsp;<icon:check className="tw-w-4 tw-h-4 tw-text-green-500" /><uv:number number="${sickDays[person].days['WITH_AUB']}"/>)
                                </c:if>
                            </div>
                            <div class="tw-flex tw-items-center">
                                <icon:child className="tw-w-3 tw-h-3" />&nbsp;<uv:number number="${childSickDays[person].days['TOTAL']}"/>
                                <c:if test="${childSickDays[person].days['WITH_AUB'] > 0}">
                                    &nbsp;(&nbsp;<icon:check className="tw-w-4 tw-h-4 tw-text-green-500"/><uv:number number="${childSickDays[person].days['WITH_AUB']}"/>)
                                </c:if>
                            </div>
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
