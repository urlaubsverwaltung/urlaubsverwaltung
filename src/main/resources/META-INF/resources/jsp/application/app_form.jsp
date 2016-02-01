<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>



<!DOCTYPE html>
<html>

<head>
    <uv:head/>
    <link rel="stylesheet" type="text/css" href="<spring:url value='/css/timepicker.css' />" />
    <script src="<spring:url value='/js/timepicker.js' />" type="text/javascript" ></script>

    <%@include file="include/app-form-elements/datepicker.jsp" %>
    <%@include file="include/app-form-elements/day-length-selector.jsp" %>

    <script type="text/javascript">
        $(function() {
            <%-- CALENDAR: PRESET DATE IN APP FORM ON CLICKING DAY --%>

            function preset(id, dateString) {

                var match = dateString.match(/\d+/g);

                var y = match[0];
                var m = match[1] - 1;
                var d = match[2];

                $(id).datepicker('setDate', new Date(y, m , d));
            }

            var from = '${param.from}';
            var to   = '${param.to}';

            if (from) {
                preset('#from', from);
                preset('#to'  , to || from);

                var urlPrefix = "<spring:url value='/api' />";
                var personId = "<c:out value='${person.id}' />";
                var startDate = $("#from").datepicker("getDate");
                var endDate = $("#to").datepicker("getDate");

                sendGetDaysRequest(urlPrefix,
                        startDate,
                        endDate,
                        $('input:radio[name=dayLength]:checked').val(),
                        personId, ".days");

                sendGetDepartmentVacationsRequest(urlPrefix, startDate, endDate, personId, "#departmentVacations");
            }

            <%-- Timepicker for optional startTime and endTime --%>

            $('#startTime').timepicker({
                'step': 15 ,
                'timeFormat': 'H:i',
                'forceRoundTime': true,
                'scrollDefault': 'now'});
            $('#endTime').timepicker({
                'step': 15 ,
                'timeFormat': 'H:i',
                'forceRoundTime': true,
                'scrollDefault': 'now'});

        });
    </script>

</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasAuthority('OFFICE')">
    <c:set var="IS_OFFICE" value="${true}"/>
</sec:authorize>

<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>

<c:set var="TIME_PATTERN">
    <spring:message code="pattern.time"/>
</c:set>

<uv:menu/>

<div class="content">

<div class="container">

<c:choose>

<c:when test="${noHolidaysAccount}">

    <spring:message code="application.applier.account.none"/>

</c:when>

<c:otherwise>

<form:form method="POST" action="${URL_PREFIX}/application" modelAttribute="application" class="form-horizontal" role="form">
<form:hidden path="person" value="${person.id}" />

<c:if test="${not empty errors}">
    <div class="row">
        <div class="col-xs-12 alert alert-danger">
            <form:errors />
        </div>
    </div>
</c:if>

<div class="row">

    <div class="form-section">
        <div class="col-xs-12">
            <legend><spring:message code="application.data.title" /></legend>
        </div>

        <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle"></i>
                <spring:message code="application.data.description"/>
            </span>
            <span id="departmentVacations" class="help-block info"></span>
        </div>

        <div class="col-md-8 col-md-pull-4">
            <c:if test="${IS_OFFICE}">
                <%-- office applies for a user --%>
                <div class="form-group">
                    <label class="control-label col-md-3">
                        <spring:message code="application.data.staff"/>
                    </label>
                    <div class="col-md-9">
                        <select id="person-select" class="form-control"
                                onchange="window.location.href=this.options[this.selectedIndex].value">
                            <c:forEach items="${persons}" var="p">
                                <c:choose>
                                    <c:when test="${person.id == p.id}">
                                        <c:set var="SELECTED_ATTRIBUTE" value="selected='selected'"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="SELECTED_ATTRIBUTE" value=""/>
                                    </c:otherwise>
                                </c:choose>
                                <option value="${URL_PREFIX}/application/new?person=${p.id}" ${SELECTED_ATTRIBUTE}>
                                    <c:out value="${p.niceName}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>
            </c:if>

            <div class="form-group is-required">
                <label class="control-label col-md-3" for="vacationType">
                    <spring:message code="application.data.vacationType"/>:
                </label>
                <div class="col-md-9">
                    <form:select path="vacationType" size="1" id="vacationType" class="form-control" onchange="vacationTypeChanged(value);">
                        <c:forEach items="${vacationTypes}" var="vacationType">
                            <c:choose>
                                <c:when test="${vacationType == application.vacationType}">
                                    <option value="${vacationType}" selected="selected">
                                        <spring:message code="${vacationType}"/>
                                    </option>
                                </c:when>
                                <c:otherwise>
                                    <option value="${vacationType}">
                                        <spring:message code="${vacationType}"/>
                                    </option>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </form:select>
                </div>
            </div>

            <div class="form-group is-required">
                <label class="control-label col-md-3">
                    <spring:message code="absence.period"/>:
                </label>
                <div class="col-md-9 radio">
                    <label class="thirds">
                        <form:radiobutton id="fullDay" class="dayLength-full" path="dayLength" checked="checked" value="FULL" />
                        <spring:message code="FULL"/>
                    </label>
                    <label class="thirds">
                        <form:radiobutton id="morning" class="dayLength-half" path="dayLength" value="MORNING" />
                        <spring:message code="MORNING"/>
                    </label>
                    <label class="thirds">
                        <form:radiobutton id="noon" class="dayLength-half" path="dayLength" value="NOON" />
                        <spring:message code="NOON"/>
                    </label>
                </div>
            </div>

            <div class="form-group is-required">
                <label class="col-md-3 control-label" for="from">
                    <spring:message code="absence.period.startDate" />:
                </label>
                <div class="col-md-5">
                    <form:input id="from" path="startDate" class="form-control" cssErrorClass="form-control error" placeholder="${DATE_PATTERN}" />
                </div>
                <div class="col-md-4">
                    <form:input id="startTime" path="startTime" class="form-control" cssErrorClass="form-control error" placeholder="${TIME_PATTERN}" />
                </div>
            </div>

            <div class="form-group is-required">
                <label class="control-label col-md-3" for="to">
                    <spring:message code="absence.period.endDate" />:
                </label>
                <div class="col-md-5">
                    <form:input id="to" path="endDate" class="form-control" cssErrorClass="form-control error" placeholder="${DATE_PATTERN}" />
                    <span class="help-block info days"></span>
                </div>
                <div class="col-md-4">
                    <form:input id="endTime" path="endTime" class="form-control" cssErrorClass="form-control error" placeholder="${TIME_PATTERN}" />
                </div>
            </div>

            <div class="form-group">
                <label class="control-label col-md-3" for="holidayReplacement">
                    <spring:message code="application.data.holidayReplacement"/>:
                </label>
                <div class="col-md-9">
                    <form:select path="holidayReplacement" id="holidayReplacement" size="1" class="form-control">
                        <option value="-1"><spring:message code="application.data.holidayReplacement.none"/></option>
                        <c:forEach items="${persons}" var="holidayReplacement">
                            <c:choose>
                                <c:when test="${application.holidayReplacement.id == holidayReplacement.id}">
                                    <form:option value="${holidayReplacement.id}" selected="selected">
                                        <c:out value="${holidayReplacement.niceName}" />
                                    </form:option>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${person.id != holidayReplacement.id}">
                                        <form:option value="${holidayReplacement.id}">
                                            <c:out value="${holidayReplacement.niceName}" />
                                        </form:option>
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </form:select>
                    <form:errors path="holidayReplacement" cssClass="error"/>
                </div>
            </div>

            <div class="form-group is-required">
                <label class="control-label col-md-3">
                    <spring:message code="application.data.teamInformed"/>:
                </label>
                <div class="col-md-9 radio">
                    <label class="halves">
                        <form:radiobutton id="teamInformed" path="teamInformed" value="true"/>
                        <spring:message code="application.data.teamInformed.true"/>
                    </label>
                    <label class="halves">
                        <form:radiobutton id="teamNotInformed" path="teamInformed" value="false"/>
                        <spring:message code="application.data.teamInformed.false"/>
                    </label>
                    <form:errors path="teamInformed" cssClass="error"/>
                </div>
            </div>
        </div>

        <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle"></i>
                <spring:message code="application.data.hours.description"/>
            </span>
        </div>

        <div class="col-md-8 col-md-pull-4">
            <c:set var="HOURS_IS_REQUIRED" value="${application.vacationType == 'OVERTIME' ? 'is-required' : ''}"/>
            <div class="form-group ${HOURS_IS_REQUIRED}" id="form-group--hours">
                <label class="control-label col-md-3" for="hours">
                    <spring:message code="application.data.hours" />:
                </label>
                <div class="col-md-9">
                    <form:input path="hours" class="form-control" cssErrorClass="form-control error" />
                    <form:errors path="hours" cssClass="error"/>
                </div>
            </div>
        </div>

    </div>

    <div class="form-section">
        <div class="col-xs-12">
            <legend><spring:message code="application.data.furtherInformation.title" /></legend>
        </div>
        <div class="col-md-4 col-md-push-8">
            <span class="help-block">
                <i class="fa fa-fw fa-info-circle"></i>
                <spring:message code="application.data.furtherInformation.description"/>
            </span>
        </div>
        <div class="col-md-8 col-md-pull-4">
            <c:set var="REASON_IS_REQUIRED" value="${application.vacationType == 'SPECIALLEAVE' ? 'is-required' : ''}"/>

            <div class="form-group ${REASON_IS_REQUIRED}" id="form-group--reason">
                <label class="control-label col-md-3" for="reason">
                    <spring:message code="application.data.reason"/>:
                </label>
                <div class="col-md-9">
                    <span id="text-reason"></span><spring:message code="action.comment.maxChars"/>
                    <form:textarea id="reason" rows="1" path="reason" class="form-control" cssErrorClass="form-control error"
                                   onkeyup="count(this.value, 'text-reason');"
                                   onkeydown="maxChars(this,200); count(this.value, 'text-reason');"/>
                    <form:errors path="reason" cssClass="error"/>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-3" for="address">
                    <spring:message code="application.data.furtherInformation.address"/>:
                </label>
                <div class="col-md-9">
                    <span id="text-address"></span><spring:message code="action.comment.maxChars"/>
                    <form:textarea id="address" rows="1" path="address" class="form-control" cssErrorClass="form-control error"
                                   onkeyup="count(this.value, 'text-address');"
                                   onkeydown="maxChars(this,200); count(this.value, 'text-address');"/>
                    <form:errors path="address" cssClass="error"/>
                </div>
            </div>
            <div class="form-group">
                <label class="control-label col-md-3" for="comment">
                    <spring:message code="application.data.furtherInformation.comment"/>:
                </label>
                <div class="col-md-9">
                    <span id="text-comment"></span><spring:message code="action.comment.maxChars"/>
                    <form:textarea id="comment" rows="1" path="comment" class="form-control" cssErrorClass="form-control error"
                                   onkeyup="count(this.value, 'text-comment');"
                                   onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
                    <form:errors path="comment" cssClass="error"/>
                </div>
            </div>
        </div>
    </div>

    <div class="form-section">
        <div class="col-xs-12">
            <hr/>
            <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
                <spring:message code="action.apply.vacation"/>
            </button>
            <button type="button" class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right">
                <spring:message code="action.cancel"/>
            </button>
        </div>
    </div>

</div>

</div>

</form:form>

</c:otherwise>
</c:choose>


</div>
<!-- End of grid container -->

</div>

</body>

</html>
