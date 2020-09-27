<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="person.form.workingTime.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='workingtime_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="content">
    <div class="container">

        <form:form method="POST" action="${URL_PREFIX}/person/${person.id}/workingtime" modelAttribute="workingTime"
                   class="form-horizontal">

            <div class="form-section">
                <uv:section-heading>
                    <h1>
                        <spring:message code="person.form.workingTime.title" arguments="${person.niceName}"/>
                    </h1>
                </uv:section-heading>

                <c:set var="workingTimeError">
                    <form:errors path="workingDays" cssClass="error"/>
                </c:set>
                <c:if test="${not empty workingTimeError}">
                <div class="row tw-mb-8">
                    <div class="col-xs-12">
                        <div class="alert alert-danger">${workingTimeError}</div>
                    </div>
                </div>
                </c:if>

                <div class="row tw-mb-16">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <uv:icon-information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="federalState.${defaultFederalState}" var="defaultFederalStateName"/>
                            <spring:message code="person.form.workingTime.federalState.description" arguments="${defaultFederalStateName}"/>
                        </span>
                        <span class="help-block tw-text-sm">
                            <uv:icon-information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="person.form.workingTime.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3" for="federalStateType">
                                <spring:message code='settings.publicHolidays.federalState'/>:
                            </label>

                            <div class="col-md-9">
                                <form:select path="federalState" id="federalStateType" class="form-control"
                                             cssErrorClass="form-control error">
                                    <form:option value=""><spring:message
                                        code="person.form.workingTime.federalState.default"
                                        arguments="${defaultFederalStateName}"/></form:option>
                                    <option disabled='true'>---------------</option>
                                    <c:forEach items="${federalStateTypes}" var="federalStateType">
                                        <form:option value="${federalStateType}"><spring:message
                                            code="federalState.${federalStateType}"/></form:option>
                                    </c:forEach>
                                </form:select>
                            </div>
                        </div>

                        <c:if test="${fn:length(workingTimes) > 1}">
                        <div class="form-group">
                            <label class="col-md-3">
                                <spring:message code='person.form.workingTime.existent'/>:
                            </label>

                            <div class="col-md-9">
                                <ul class="tw-list-none tw-m-0 tw-p-0 tw-space-y-2 tw-text-sm">
                                    <c:forEach items="${workingTimes}" var="time">
                                        <li>
                                            <div>
                                                <spring:message code="person.form.workingTime.validityPeriod"/>
                                                <uv:date date="${time.validFrom}"/>
                                            </div>
                                            <c:if test="${time.monday.duration > 0}">
                                                <spring:message code="MONDAY"/>,
                                            </c:if>
                                            <c:if test="${time.tuesday.duration > 0}">
                                                <spring:message code="TUESDAY"/>,
                                            </c:if>
                                            <c:if test="${time.wednesday.duration > 0}">
                                                <spring:message code="WEDNESDAY"/>,
                                            </c:if>
                                            <c:if test="${time.thursday.duration > 0}">
                                                <spring:message code="THURSDAY"/>,
                                            </c:if>
                                            <c:if test="${time.friday.duration > 0}">
                                                <spring:message code="FRIDAY"/>,
                                            </c:if>
                                            <c:if test="${time.saturday.duration > 0}">
                                                <spring:message code="SATURDAY"/>,
                                            </c:if>
                                            <c:if test="${time.sunday.duration > 0}">
                                                <spring:message code="SUNDAY"/>
                                            </c:if>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </div>
                        </div>
                        </c:if>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3">
                                <spring:message code="person.form.workingTime.validityPeriod"/>:
                            </label>
                            <div class="col-md-9">
                                <c:set var="DATE_PATTERN">
                                    <spring:message code="pattern.date"/>
                                </c:set>
                                <form:input id="validFrom" path="validFrom" class="form-control"
                                            cssErrorClass="form-control error"
                                            placeholder="${DATE_PATTERN}"/>
                                <span class="help-inline"><form:errors path="validFrom" cssClass="error"/></span>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3">
                                <spring:message code="person.form.workingTime.weekDays"/>:
                            </label>
                            <div class="col-md-9">
                                <c:forEach items="${weekDays}" var="weekDay">
                                    <div class="checkbox">
                                        <label for="${weekDay}">
                                            <form:checkbox id="${weekDay}" path="workingDays"
                                                           value="${weekDay.dayOfWeek}"/>
                                            <spring:message code='${weekDay}'/>
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <button class="btn btn-success col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                            code="action.save"/></button>
                        <button type="button" class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right">
                            <spring:message code="action.cancel"/>
                        </button>
                    </div>
                </div>
            </div>

        </form:form>

    </div>
</div>

</body>

</html>
