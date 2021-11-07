<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri = "/WEB-INF/asset.tld"%>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="person.form.workingTime.header.title" arguments="${person.niceName}"/>
    </title>
    <uv:custom-head/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.css' />" />
    <link rel="stylesheet" type="text/css" href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />" />
    <script>
        window.uv = {};
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
    </script>
    <uv:datepicker-localisation />
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~95889e93.js' />"></script>
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
                    <form:errors path="workingDays" />
                </c:set>
                <c:if test="${not empty workingTimeError}">
                <div class="row tw-mb-8">
                    <div class="col-xs-12">
                        <div class="alert alert-danger tw-text-red-800">${workingTimeError}</div>
                    </div>
                </div>
                </c:if>

                <div class="row tw-mb-16">
                    <div class="col-md-4 col-md-push-8">
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="federalState.${defaultFederalState}" var="defaultFederalStateName"/>
                            <c:choose>
                                <c:when test="${defaultFederalState == 'NONE'}">
                                    <c:set var="defaultFederalCountryAndStateName">${defaultFederalStateName}</c:set>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="country.${defaultFederalState.country}" var="defaultCountryName"/>
                                    <c:set var="defaultFederalCountryAndStateName">${defaultCountryName} - ${defaultFederalStateName}</c:set>
                                </c:otherwise>
                            </c:choose>
                            <spring:message code="person.form.workingTime.federalState.description" arguments="${defaultFederalCountryAndStateName}"/>
                        </span>
                        <span class="help-block tw-text-sm">
                            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                            <spring:message code="person.form.workingTime.description"/>
                        </span>
                    </div>

                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group">
                            <label class="control-label col-md-3" for="federalStateType">
                                <spring:message code='settings.publicHolidays.federalState'/>:
                            </label>

                            <div class="col-md-9">
                                <uv:select id="federalStateType" name="federalState">
                                    <c:set var="countryLabelGeneral"><spring:message code="country.general"/></c:set>
                                    <optgroup label="${countryLabelGeneral}">
                                        <option value="" ${workingTime.defaultFederalState == true ? 'selected="selected"' : ''}>
                                            <spring:message code="person.form.workingTime.federalState.default" arguments="${defaultFederalCountryAndStateName}" />
                                        </option>
                                        <option value="NONE" ${workingTime.defaultFederalState == false && workingTime.federalState == "NONE" ? 'selected="selected"' : ''}>
                                            <spring:message code="federalState.NONE"/>
                                        </option>
                                    </optgroup>
                                    <c:forEach items="${federalStateTypes}" var="federalStatesByCountry">
                                        <c:set var="countryLabel"><spring:message code="country.${federalStatesByCountry.key}" /></c:set>
                                        <optgroup label="${countryLabel}">
                                            <c:forEach items="${federalStatesByCountry.value}" var="federalStateType">
                                                <option value="${federalStateType}" ${workingTime.defaultFederalState == false && workingTime.federalState == federalStateType ? 'selected="selected"' : ''}>
                                                    <spring:message code="federalState.${federalStateType}"/>
                                                </option>
                                            </c:forEach>
                                        </optgroup>
                                    </c:forEach>
                                </uv:select>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-3">
                                <spring:message code="person.form.workingTime.valid-from"/>:
                            </label>
                            <div class="col-md-9">
                                <c:set var="DATE_PATTERN">
                                    <spring:message code="pattern.date"/>
                                </c:set>
                                <form:input id="validFrom" path="validFrom" class="form-control"
                                            cssErrorClass="form-control error" placeholder="${DATE_PATTERN}"
                                            data-iso-value="${workingTime.validFromIsoValue}" />
                                <uv:error-text>
                                    <form:errors path="validFrom" />
                                </uv:error-text>
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
                                                           value="${weekDay.value}"/>
                                            <spring:message code='${weekDay}'/>
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <c:if test="${fn:length(workingTimeHistories) > 0}">
                            <div class="form-group">
                                <label class="col-md-3">
                                    <spring:message code='person.form.workingTime.existent'/>:
                                </label>

                                <div class="col-md-9">
                                    <ul class="tw-list-none tw-m-0 tw-p-0 tw-space-y-2 tw-text-sm">
                                        <c:forEach items="${workingTimeHistories}" var="workingTimeHistory" varStatus="status">
                                            <li>

                                                <c:choose>
                                                    <c:when test="${status.first}">
                                                        <span class="tw-block ${workingTimeHistory.valid ? 'tw-text-emerald-500' : ''}">
                                                            <spring:message code="person.form.workingTime.valid-from"/>
                                                            <uv:date date="${workingTimeHistory.validFrom}"/>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="tw-block ${workingTimeHistory.valid ? 'tw-text-emerald-500' : ''}">
                                                            <c:set var="actualDate">
                                                                <uv:date date="${workingTimeHistory.validFrom}"/>
                                                            </c:set>
                                                            <spring:message code="person.form.workingTime.valid-from-to" arguments="${actualDate}, ${lastDate}"/>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>

                                                <c:forEach items="${workingTimeHistory.workingDays}" var="workingDay" varStatus="loop">
                                                    <spring:message code="${workingDay}"/>${loop.last ? '' : ','}
                                                </c:forEach>

                                                <span class="tw-block">
                                                    <c:choose>
                                                        <c:when test="${workingTimeHistory.federalState == 'NONE'}">
                                                            <spring:message code="federalState.NONE"/>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <spring:message code="country.${workingTimeHistory.country}"/> - <spring:message code="federalState.${workingTimeHistory.federalState}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>

                                                <c:set var="lastDate"><uv:date date="${workingTimeHistory.validFrom.minusDays(1)}"/></c:set>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <hr />
                        <button class="button-main-green dark:button-main col-xs-12 col-sm-5 col-md-2" type="submit"><spring:message
                            code="action.save"/></button>
                        <button type="button" class="button col-xs-12 col-sm-5 col-md-2 pull-right" data-back-button>
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
