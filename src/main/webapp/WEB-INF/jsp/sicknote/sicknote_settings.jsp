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
        <form:form method="POST" action="${URL_PREFIX}/sicknote/settings" modelAttribute="sickNoteSettings" class="form-horizontal"
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

            <div class="row">
                <div class="col-xs-12">
                    <div class="form-section">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="settings.sickDays.title"/>
                            </h2>
                        </uv:section-heading>
                        <div class="row">
                            <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />

                                    <spring:message code="settings.sickDays.description"/>
                                </span>
                            </div>
                            <div class="col-md-8 col-md-pull-4">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4" for="sickNoteSettings.maximumSickPayDays">
                                        <spring:message code='settings.sickDays.maximumSickPayDays'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="sickNoteSettings.maximumSickPayDays"
                                                    path="maximumSickPayDays" class="form-control"
                                                    cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="maximumSickPayDays" />
                                        </uv:error-text>
                                    </div>
                                </div>
                                <div class="form-group is-required">
                                    <label class="control-label col-md-4"
                                           for="sickNoteSettings.daysBeforeEndOfSickPayNotification">
                                        <spring:message code='settings.sickDays.daysBeforeEndOfSickPayNotification'/>:
                                    </label>
                                    <div class="col-md-8">
                                        <form:input id="sickNoteSettings.daysBeforeEndOfSickPayNotification"
                                                    path="daysBeforeEndOfSickPayNotification"
                                                    class="form-control" cssErrorClass="form-control error"
                                                    type="number" step="1"/>
                                        <uv:error-text>
                                            <form:errors path="daysBeforeEndOfSickPayNotification" />
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <p class="help-block tw-text-sm"><spring:message code="settings.action.update.description"/></p>
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
