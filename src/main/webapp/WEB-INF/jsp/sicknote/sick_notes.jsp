
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
    <script type="text/javascript">
        $(document).ready(function() {

            $("table.sortable").tablesorter({
                sortList: [[1,0]]
            });
            
        });
        
    </script>
</head>

<body>

<spring:url var="formUrlPrefix" value="/web" />

<uv:menu />

<div class="print-info--only-landscape">
    <h4><spring:message code="print.info.landscape" /></h4>
</div>

<div class="content print--only-landscape">
    <div class="container">

        <div class="row">

            <div class="col-xs-12">

            <div class="header">

                <legend class="sticky">
                    <p>
                        <spring:message code="sicknotes" />
                        <c:if test="${person != null}">
                            <spring:message code="for" />&nbsp;<c:out value="${person.niceName}" />
                        </c:if>
                    </p>
                    <div class="btn-group btn-group-legend pull-right hidden-xs hidden-sm">
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
                        <i class="fa fa-plus"></i> <span class="hidden-xs"><spring:message code="sicknotes.new" /></span>
                    </a>
                    <a href="#changeViewModal" role="button" class="btn btn-default pull-right hidden-xs hidden-sm" data-toggle="modal">
                        <i class="fa fa-filter"></i>&nbsp;<spring:message code="filter" />
                    </a>
                </legend>

            </div>

            <div id="changeViewModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                            <h4 id="myModalLabel" class="modal-title"><spring:message code="filter" /></h4>
                        </div>
                        <form:form method="POST" id="searchRequest-form" action="${formUrlPrefix}/sicknote/filter" modelAttribute="searchRequest" class="form-horizontal">
                            <div class="modal-body">

                                <div class="form-group">
                                    <label class="control-label col-sm-4">
                                        <spring:message code="time" />
                                    </label>
                                    <div class="col-sm-7 radio">
                                        <label class="thirds">
                                            <form:radiobutton id="periodYear" path="period" value="YEAR" checked="checked" />
                                            <spring:message code="period.year" />
                                        </label>
                                        <label class="thirds">
                                            <form:radiobutton id="periodQuartal" path="period" value="QUARTAL" />
                                            <spring:message code="period.quartal" />
                                        </label>
                                        <label class="thirds">
                                            <form:radiobutton id="periodMonth" path="period" value="MONTH" />
                                            <spring:message code="period.month" />
                                        </label>
                                    </div>
                                </div>

                            </div>
                            <div class="modal-footer">
                                <button class="btn btn-primary is-sticky" type="submit"><i class="fa fa-check"></i> <spring:message code="go" /></button>
                                <button class="btn btn-default is-sticky" data-dismiss="modal" aria-hidden="true"><i class="fa fa-remove"></i> <spring:message code="cancel" /></button>
                            </div>
                        </form:form>
                    </div>
                </div>
            </div>

            <div>
                <p class="is-inline-block">
                    <spring:message code="time"/>:&nbsp;<uv:date date="${from}" /> - <uv:date date="${to}" />
                </p>
                <p class="pull-right visible-print">
                    <spring:message code="Effective"/> <uv:date date="${today}" />
                </p>
            </div>
                <table class="list-table selectable-table sortable tablesorter" cellspacing="0">
                    <thead class="hidden-xs hidden-sm">
                    <tr>
                        <th class="hidden-print"></th>
                        <th class="sortable-field"><spring:message code="firstname"/></th>
                        <th class="sortable-field"><spring:message code="lastname"/></th>
                        <th class="sortable-field"><spring:message code="sicknotes.days.number"/></th>
                        <th class="sortable-field"><spring:message code="sicknotes.child.days.number"/></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${persons}" var="person">
                    <tr onclick="navigate('${formUrlPrefix}/staff/${person.id}/overview#anchorSickNotes');">
                        <td class="is-centered hidden-print">
                            <img class="img-circle hidden-print" src="<c:out value='${gravatars[person]}?d=mm&s=60'/>"/>
                        </td>
                        <td class="hidden-xs">
                            <c:out value="${person.firstName}"/>
                        </td>
                        <td class="hidden-xs">
                            <c:out value="${person.lastName}"/>
                        </td>
                        <td class="visible-xs">
                            <c:out value="${person.firstName}"/> <c:out value="${person.lastName}"/>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-medkit hidden-print"></i>
                            <fmt:formatNumber value="${sickDays[person]}"/> <spring:message
                                    code="sicknotes.days"/>
                            <c:if test="${sickDaysWithAUB[person] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check check"></i> <spring:message
                                        code="overview.sicknotes.sickdays.aub" arguments="${sickDaysWithAUB[person]}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="hidden-xs">
                            <i class="fa fa-child hidden-print"></i> <fmt:formatNumber value="${childSickDays[person]}"/>
                                <spring:message code="sicknotes.child.days"/>
                            <c:if test="${childSickDaysWithAUB[person] > 0}">
                                <p class="list-table--second-row">
                                    <i class="fa fa-check check"></i> <spring:message
                                            code="overview.sicknotes.sickdays.aub"
                                            arguments="${childSickDaysWithAUB[person]}"/>
                                </p>
                            </c:if>
                        </td>
                        <td class="visible-xs">
                            <i class="fa fa-medkit hidden-print"></i> <fmt:formatNumber value="${sickDays[person]}"/>
                            <i class="fa fa-child hidden-print"></i> <fmt:formatNumber
                                value="${childSickDays[person]}"/>
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
