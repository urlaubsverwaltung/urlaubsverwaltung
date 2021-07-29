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
        <form:form method="POST" action="${URL_PREFIX}/calendar/settings" modelAttribute="calendarSettings" class="form-horizontal"
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
                <div class="alert alert-danger tw-flex tw-items-center" role="alert">
                    <icon:speakerphone className="tw-w-4 tw-h-4" solid="true" />
                    &nbsp;<spring:message code="settings.calendar.deprecated"/>
                </div>
                <uv:section-heading>
                    <h2>
                        <spring:message code="settings.calendar.title"/>
                    </h2>
                </uv:section-heading>

                    <div class="form-section">
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
                                        <uv:select id="calendarSettingsProvider" name="provider">
                                            <c:forEach items="${calendarProviders}" var="provider">
                                                <option value="${provider}" ${calendarSettings.provider == provider ? 'selected="selected"' : ''}>
                                                    <spring:message code="settings.calendar.provider.${provider}"/>
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section" id="exchange-calendar">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.calendar.ews.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.calendar.ews.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.email">
                                        <spring:message code='settings.calendar.ews.email'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.exchangeCalendarSettings.email"
                                                    path="exchangeCalendarSettings.email"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="exchangeCalendarSettings.email" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.password">
                                        <spring:message code='settings.calendar.ews.password'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:password showPassword="true"
                                                       id="calendarSettings.exchangeCalendarSettings.password"
                                                       path="exchangeCalendarSettings.password"
                                                       class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="exchangeCalendarSettings.password" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.ewsUrl">
                                        <spring:message code='settings.calendar.ews.url'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.exchangeCalendarSettings.ewsUrl"
                                                    path="exchangeCalendarSettings.ewsUrl"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="exchangeCalendarSettings.ewsUrl" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.calendar">
                                        <spring:message code='settings.calendar.ews.calendar'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="calendarSettings.exchangeCalendarSettings.calendar"
                                                    path="exchangeCalendarSettings.calendar"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="exchangeCalendarSettings.calendar" />
                                        </uv:error-text>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.timeZoneId">
                                        <spring:message code='settings.calendar.ews.timeZoneId'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <uv:select id="calendarSettings.exchangeCalendarSettings.timeZoneId" name="exchangeCalendarSettings.timeZoneId"
                                                   cssClass="chosenCombo">
                                            <c:forEach items="${calendarSettings.availableTimezones}" var="timeZoneId">
                                                <option value="${timeZoneId}" ${calendarSettings.exchangeCalendarSettings.timeZoneId == timeZoneId ? 'selected="selected"' : ''}>
                                                        ${timeZoneId}
                                                </option>
                                            </c:forEach>
                                        </uv:select>
                                        <uv:error-text>
                                            <form:errors path="provider" />
                                        </uv:error-text>
                                    </div>
                                </div>

                                <div class="form-group">
                                    <label class="control-label col-md-4"
                                           for="calendarSettings.exchangeCalendarSettings.sendInvitationActive">
                                        <spring:message code='settings.calendar.ews.notification'/>:
                                    </label>
                                    <div class="col-md-8 checkbox">
                                        <label>
                                            <form:checkbox
                                                id="calendarSettings.exchangeCalendarSettings.sendInvitationActive"
                                                path="exchangeCalendarSettings.sendInvitationActive"
                                                value="true"/>
                                            <spring:message code="settings.calendar.ews.notification.true"/>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section" id="google-calendar">
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
                                                    path="googleCalendarSettings.clientId"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="googleCalendarSettings.clientId" />
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
                                                    path="googleCalendarSettings.clientSecret"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="googleCalendarSettings.clientSecret" />
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
                                                    path="googleCalendarSettings.calendarId"
                                                    class="form-control" cssErrorClass="form-control error"/>
                                        <uv:error-text>
                                            <form:errors path="googleCalendarSettings.calendarId" />
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
                                               value="${calendarSettings.googleCalendarSettings.authorizedRedirectUrl}" readonly/>
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
                                            test="${calendarSettings.googleCalendarSettings.refreshToken == null}">
                                            <p class="text-danger col-md-5 col-md-push-4"><spring:message
                                                code="settings.calendar.google.action.authenticate.description"/></p>
                                            <button id="googleOAuthButton" value="oauth" name="googleOAuthButton"
                                                    type="submit" class="btn btn-primary col-md-3 col-md-push-4">
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
