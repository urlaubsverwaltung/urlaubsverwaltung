
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>


<!DOCTYPE html>
<html>

<head>
    <uv:head />
    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var regional = "${pageContext.request.locale.language}";

            createDatepickerInstanceForSickNote(regional, "from", "to");

            $("table.sortable").tablesorter({
                sortList: [[0,0]],
                headers: { 
                    0: { sorter:'germanDate' },
                    3: { sorter:'germanDate' },
                    5: { sorter:'germanDate' }
                }
            });
            
        });
        
    </script>
</head>

<body>

<spring:url var="formUrlPrefix" value="/web" />

<uv:menu />

<div class="content">
    <div class="grid-container">

        <div class="grid-100">

            <div class="header">

                <legend class="sticky">
                    <p>
                        <spring:message code="sicknotes" />
                        <c:if test="${person != null}">
                            <spring:message code="for" />&nbsp;<c:out value="${person.firstName}" />&nbsp;<c:out value="${person.lastName}" />
                        </c:if>
                    </p>
                    <div class="btn-group btn-group-legend pull-right">
                        <a class="btn btn-default dropdown-toggle" data-toggle="dropdown" href="#">
                            <i class="fa fa-bar-chart"></i>&nbsp;<spring:message code='sicknotes.statistics.short' />
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu">
                            <c:forEach begin="0" end="10" varStatus="counter">
                                <li>
                                    <a href="${formUrlPrefix}/sicknote/statistics?year=${today.year - counter.index}">
                                        <c:out value="${today.year - counter.index}" />
                                    </a>
                                </li> 
                            </c:forEach>
                        </ul>
                    </div>
                    <uv:print />
                    <a class="btn btn-default pull-right" href="${formUrlPrefix}/sicknote/new">
                        <i class="fa fa-plus"></i>&nbsp;<spring:message code="sicknotes.new" />
                    </a>
                    <a href="#changeViewModal" role="button" class="btn btn-default pull-right" data-toggle="modal">
                        <i class="fa fa-filter"></i>&nbsp;<spring:message code="filter" />
                    </a>
                </legend>

            </div>

            <div id="changeViewModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3 id="myModalLabel"><spring:message code="filter" /></h3>
                </div>
                <form:form method="POST" id="searchRequest-form" action="${formUrlPrefix}/sicknote/filter" modelAttribute="searchRequest" class="form-horizontal stretched">
                <div class="modal-body">

                    <div class="control-group">
                        <label class="control-label" for="employee"><spring:message code="staff" /></label>

                        <div class="controls">
                            <form:select path="personId" id="employee" cssErrorClass="error">
                                <form:option value="-1"><spring:message code="staff.all" /></form:option>
                                <c:forEach items="${persons}" var="person">
                                    <form:option value="${person.id}">${person.niceName}</form:option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>
                    
                    <div class="control-group">
                        <label class="control-label">
                            <spring:message code="time" />
                        </label>
                        <div class="controls radiobuttons">
                            <form:radiobutton id="periodYear" path="period" value="YEAR" checked="checked" />
                            <label for="periodYear"><spring:message code="period.year" /></label>
                            <form:radiobutton id="periodQuartal" path="period" value="QUARTAL" />
                            <label for="periodQuartal"><spring:message code="period.quartal" /></label>
                            <form:radiobutton id="periodMonth" path="period" value="MONTH" />
                            <label for="periodMonth"><spring:message code="period.month" /></label>
                        </div>
                    </div>

                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" type="submit"><spring:message code="go" /></button>
                    <button class="btn btn-default" data-dismiss="modal" aria-hidden="true"><spring:message code="cancel" /></button>
                </div>
                </form:form>
            </div>

            <div>
                <p style="display:inline-block">
                    <spring:message code="time"/>:&nbsp;<uv:date date="${from}" /> - <uv:date date="${to}" />
                </p>
                <p style="float:right;">
                    <spring:message code="Effective"/> <uv:date date="${today}" />
                </p>
            </div>

            <c:choose>

                <c:when test="${empty sickNotes}">
                    <div>
                        <spring:message code="sicknotes.none" />
                    </div>
                </c:when>

                <c:otherwise>
                    <table class="data-table is-centered sortable tablesorter zebra-table" cellspacing="0">
                        <thead>
                        <tr>
                            <th class="print--invisible"><spring:message code="app.date.overview" /></th>
                            <th><spring:message code="firstname" /></th>
                            <th><spring:message code="lastname" /></th>
                            <th><spring:message code="sicknotes.time" /></th>
                            <th><spring:message code="work.days" /></th>
                            <th><spring:message code="sicknotes.aub.short" /></th>
                            <th class="print--invisible"><spring:message code="edit" /></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
                            <c:choose>
                                <c:when test="${sickNote.active}">
                                    <c:set var="CSS_CLASS" value="active" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="CSS_CLASS" value="inactive" />
                                </c:otherwise>
                            </c:choose>
                            <tr class="${CSS_CLASS}" onclick="navigate('${formUrlPrefix}/sicknote/${sickNote.id}');">
                                <td class="print--invisible">
                                    <uv:date date="${sickNote.lastEdited}" />
                                </td>
                                <td>
                                    <c:out value="${sickNote.person.firstName}" />
                                </td>
                                <td>
                                    <c:out value="${sickNote.person.lastName}" />
                                </td>
                                <td>
                                    <uv:date date="${sickNote.startDate}" /> - <uv:date date="${sickNote.endDate}" />
                                </td>
                                <td>
                                    <fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" />
                                </td>
                                <td>
                                    <uv:date date="${sickNote.aubStartDate}" /> - <uv:date date="${sickNote.aubEndDate}" />
                                </td>
                                <td class="print--invisible">
                                    <c:if test="${sickNote.active}">
                                        <a href="${formUrlPrefix}/sicknote/${sickNote.id}/edit">
                                            <i class="fa fa-pencil fa-action"></i>
                                        </a>
                                    </c:if>
                                </td>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>

            </c:choose>

        </div>
    </div>
</div>

</body>

</html>
