<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<div class="row">
    <div class="col-xs-12">
        <h2><spring:message code="settings.calendar.google.title"/></h2>
    </div>
</div>
<div class="row">
    <div class="col-md-4 col-md-push-8">
        <span class="help-block tw-text-sm">
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
            <spring:message code="settings.calendar.google.description"/>
        </span>
    </div>
    <div class="col-md-8 col-md-pull-4">
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.googleCalendarSettings.clientId">
                <spring:message code='settings.calendar.google.clientid'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.googleCalendarSettings.clientId"
                            path="calendarSettings.googleCalendarSettings.clientId"
                            class="form-control" cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.googleCalendarSettings.clientId" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.googleCalendarSettings.clientSecret">
                <spring:message code='settings.calendar.google.clientsecret'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.googleCalendarSettings.clientSecret"
                            path="calendarSettings.googleCalendarSettings.clientSecret"
                            class="form-control" cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.googleCalendarSettings.clientSecret" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group">
            <label class="control-label col-md-4"
                   for="calendarSettings.googleCalendarSettings.calendarId">
                <spring:message code='settings.calendar.google.calendarid'/>:
            </label>
            <div class="col-md-8">
                <form:input id="calendarSettings.googleCalendarSettings.calendarId"
                            path="calendarSettings.googleCalendarSettings.calendarId"
                            class="form-control" cssErrorClass="form-control error"/>
                <uv:error-text>
                    <form:errors path="calendarSettings.googleCalendarSettings.calendarId" />
                </uv:error-text>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label col-md-4">
                <spring:message code='settings.calendar.google.redirecturl'/>:
            </label>
            <div class="col-md-8">
                <input class="form-control" type="text"
                       name="calendarSettings.googleCalendarSettings.authorizedRedirectUrl"
                       value="${authorizedRedirectUrl}" readonly/>
            </div>
        </div>

        <div class="form-group">
            <c:if test="${not empty oautherrors}">
                <p class="text-danger col-md-8 col-md-push-4">
                    ${oautherrors}
                </p>
            </c:if>

            <c:choose>
                <c:when
                    test="${settings.calendarSettings.googleCalendarSettings.refreshToken == null}">
                    <p class="text-danger col-md-5 col-md-push-4"><spring:message
                        code="settings.calendar.google.action.authenticate.description"/></p>
                    <button id="googleOAuthButton" value="oauth" name="googleOAuthButton"
                            type="submit" class="button-main col-md-3 col-md-push-4">
                        <spring:message code='settings.calendar.google.action.authenticate'/>
                    </button>
                </c:when>
                <c:otherwise>
                    <p class="text-success col-md-8 col-md-push-4"><spring:message
                        code="settings.calendar.google.action.authenticate.success"/></p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>
