<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <spring:message code="settings.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value='settings_form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<h1 class="tw-sr-only"><spring:message code="settings.header.title" /></h1>

<div class="content">
    <div class="container">
        <form:form method="POST" action="${URL_PREFIX}/workingtime/settings" modelAttribute="workingTimeSettings" class="form-horizontal"
                   role="form">
            <form:hidden path="id"/>
            <button type="submit" hidden></button>

            <div class="row tw-mb-4">
                <div class="col-xs-12 feedback">
                    <c:if test="${not empty errors}">
                        <div class="alert alert-danger">
                            <spring:message code="settings.action.update.error"/>
                        </div>
                    </c:if>
                    <c:if test="${success}">
                        <div class="alert alert-success">
                            <spring:message code="settings.action.update.success"/>
                        </div>
                    </c:if>
                </div>
            </div>

            <div class="form-section tw-mb-8">
                <uv:section-heading>
                    <h2>
                        <spring:message code="settings.workingTime.title"/>
                    </h2>
                </uv:section-heading>
                <div class="row">

                    <c:if test="${workingTimeSettings.defaultWorkingDaysDeactivated}">

                        <div class="col-md-4 col-md-push-8">
                            <span class="help-block tw-text-sm">
                                <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                <spring:message code="settings.workingTime.description"/>
                            </span>
                        </div>
                        <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4 tw-mb-4">
                                <spring:message code="settings.workingTime.weekdays"/>:
                            </label>
                            <div class="col-md-8">
                                <ul class="tw-list-none tw-m-0 tw-p-0 tw-grid xs:tw-grid-cols-2 tw-gap-2 md:tw-gap-1">
                                    <li class="tw-col-start-1 tw-row-start-1">
                                        <c:set var="labelMonday"><spring:message code="MONDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelMonday}"
                                            name="workingDays"
                                            value="1"
                                            checked="${workingTimeSettings.workingDays[0] == 1}"
                                        />
                                    </li>
                                    <li class="tw-col-start-1 tw-row-start-2">
                                        <c:set var="labelTuesday"><spring:message code="TUESDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelTuesday}"
                                            name="workingDays"
                                            value="2"
                                            checked="${workingTimeSettings.workingDays[1] == 2}"
                                        />
                                    </li>
                                    <li class="tw-col-start-1 tw-row-start-3">
                                        <c:set var="labelWednesday"><spring:message code="WEDNESDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelWednesday}"
                                            name="workingDays"
                                            value="3"
                                            checked="${workingTimeSettings.workingDays[2] == 3}"
                                        />
                                    </li>
                                    <li class="tw-col-start-1 tw-row-start-4">
                                        <c:set var="labelThursday"><spring:message code="THURSDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelThursday}"
                                            name="workingDays"
                                            value="4"
                                            checked="${workingTimeSettings.workingDays[3] == 4}"
                                        />
                                    </li>
                                    <li class="tw-col-start-1 tw-row-start-5">
                                        <c:set var="labelFriday"><spring:message code="FRIDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelFriday}"
                                            name="workingDays"
                                            value="5"
                                            checked="${workingTimeSettings.workingDays[4] == 5}"
                                        />
                                    </li>
                                    <li class="xs:tw-col-start-2 xs:tw-row-start-1">
                                        <c:set var="labelSaturday"><spring:message code="SATURDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelSaturday}"
                                            name="workingDays"
                                            value="6"
                                            checked="${workingTimeSettings.workingDays[5] == 6}"
                                        />
                                    </li>
                                    <li class="xs:tw-col-start-2 xs:tw-row-start-2">
                                        <c:set var="labelSunday"><spring:message code="SUNDAY" /></c:set>
                                        <uv:checkbox
                                            label="${labelSunday}"
                                            name="workingDays"
                                            value="7"
                                            checked="${workingTimeSettings.workingDays[6] == 7}"
                                        />
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>

                    </c:if>
                </div>
            </div>
            <div class="form-section tw-mb-8">
                <uv:section-heading>
                    <h2>
                        <spring:message code="settings.publicHolidays.title"/>
                    </h2>
                </uv:section-heading>
                <div class="row">
                    <div class="col-md-4 col-md-push-8">
                    <span class="help-block tw-text-sm">
                        <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                        <spring:message code="settings.publicHolidays.description"/>
                    </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">
                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="workingDurationForChristmasEve">
                                <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
                            </label>

                            <div class="col-md-8">
                                <uv:select id="dayLengthTypesChristmasEve" name="workingDurationForChristmasEve">
                                    <c:forEach items="${workingTimeSettings.dayLengthTypes}" var="dayLengthType">
                                        <option value="${dayLengthType}" ${workingTimeSettings.workingDurationForChristmasEve == dayLengthType ? 'selected="selected"' : ''}>
                                            <spring:message code="${dayLengthType}"/>
                                        </option>
                                    </c:forEach>
                                </uv:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4"
                                   for="workingDurationForNewYearsEve">
                                <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
                            </label>

                            <div class="col-md-8">
                                <uv:select id="dayLengthTypesNewYearsEve" name="workingDurationForNewYearsEve">
                                    <c:forEach items="${workingTimeSettings.dayLengthTypes}" var="dayLengthType">
                                        <option value="${dayLengthType}" ${workingTimeSettings.workingDurationForNewYearsEve == dayLengthType ? 'selected="selected"' : ''}>
                                            <spring:message code="${dayLengthType}"/>
                                        </option>
                                    </c:forEach>
                                </uv:select>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="federalStateType">
                                <spring:message code='settings.publicHolidays.federalState'/>:
                            </label>

                            <div class="col-md-8">
                                <uv:select id="federalStateType" name="federalState">
                                    <c:forEach items="${workingTimeSettings.federalStateTypes}" var="federalStateType">
                                        <option value="${federalStateType}" ${workingTimeSettings.federalState == federalStateType ? 'selected="selected"' : ''}>
                                            <spring:message code="federalState.${federalStateType}"/>
                                        </option>
                                    </c:forEach>
                                </uv:select>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <button type="submit" class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2" data-test-id="settings-save-button">
                            <spring:message code='action.save'/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

</body>

</html>
