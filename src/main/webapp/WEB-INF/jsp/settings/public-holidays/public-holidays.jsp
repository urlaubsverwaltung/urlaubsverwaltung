<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

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
                   for="workingTimeSettings.workingDurationForChristmasEve">
                <spring:message code='settings.publicHolidays.workingDuration.christmasEve'/>:
            </label>
            <div class="col-md-8">
                <uv:select id="dayLengthTypesChristmasEve" name="workingTimeSettings.workingDurationForChristmasEve">
                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                        <option value="${dayLengthType}" ${settings.workingTimeSettings.workingDurationForChristmasEve == dayLengthType ? 'selected="selected"' : ''}>
                            <spring:message code="${dayLengthType}"/>
                        </option>
                    </c:forEach>
                </uv:select>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4"
                   for="workingTimeSettings.workingDurationForNewYearsEve">
                <spring:message code='settings.publicHolidays.workingDuration.newYearsEve'/>:
            </label>
            <div class="col-md-8">
                <uv:select id="dayLengthTypesNewYearsEve" name="workingTimeSettings.workingDurationForNewYearsEve">
                    <c:forEach items="${dayLengthTypes}" var="dayLengthType">
                        <option value="${dayLengthType}" ${settings.workingTimeSettings.workingDurationForNewYearsEve == dayLengthType ? 'selected="selected"' : ''}>
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
                <uv:select id="federalStateType" name="workingTimeSettings.federalState">
                    <c:set var="countryLabelGeneral"><spring:message code="country.general" /></c:set>
                    <optgroup label="${countryLabelGeneral}">
                        <option value="NONE" ${settings.workingTimeSettings.federalState == "NONE" ? 'selected="selected"' : ''}>
                            <spring:message code="federalState.NONE"/>
                        </option>
                    </optgroup>
                    <c:forEach items="${federalStateTypes}" var="federalStatesByCountry">
                        <c:set var="countryLabel"><spring:message code="country.${federalStatesByCountry.key}" /></c:set>
                        <optgroup label="${countryLabel}">
                            <c:forEach items="${federalStatesByCountry.value}" var="federalStateType">
                                <option value="${federalStateType}" ${settings.workingTimeSettings.federalState == federalStateType ? 'selected="selected"' : ''}>
                                    <spring:message code="federalState.${federalStateType}"/>
                                </option>
                            </c:forEach>
                        </optgroup>
                    </c:forEach>
                </uv:select>
            </div>
        </div>
    </div>
</div>
