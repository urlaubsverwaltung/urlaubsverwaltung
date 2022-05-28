<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code="settings.calendar.title"/>
    </h2>
</uv:section-heading>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.calendar.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="calendarSettingsProvider">
                <spring:message code='settings.calendar.provider'/>:
            </label>
            <div class="col-md-8">
                <uv:select id="calendarSettingsProvider" name="calendarSettings.provider">
                    <c:forEach items="${providers}" var="provider">
                        <option value="${provider}" ${settings.calendarSettings.provider == provider ? 'selected="selected"' : ''}>
                            <spring:message code="settings.calendar.provider.${provider}"/>
                        </option>
                    </c:forEach>
                </uv:select>
            </div>
        </div>
    </div>
</div>
