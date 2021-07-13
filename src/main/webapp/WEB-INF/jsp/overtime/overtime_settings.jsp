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
        <form:form method="POST" action="${URL_PREFIX}/overtime/settings" modelAttribute="overtimeSettings" class="form-horizontal"
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
                        <spring:message code="settings.overtime.title"/>
                    </h2>
                </uv:section-heading>
                <div class="row">
                    <div class="col-md-4 col-md-push-8">
                                <span class="help-block tw-text-sm">
                                    <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
                                    <spring:message code="settings.overtime.description"/>
                                </span>
                    </div>
                    <div class="col-md-8 col-md-pull-4">

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                <spring:message code='settings.overtime.overtimeActive'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeActive.true"
                                                      path="overtimeActive" value="true"
                                                      data-test-id="setting-overtime-enabled" />
                                    <spring:message code="settings.overtime.overtimeActive.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeActive.false"
                                                      path="overtimeActive" value="false"
                                                      data-test-id="setting-overtime-disabled" />
                                    <spring:message code="settings.overtime.overtimeActive.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                <spring:message code='settings.overtime.overtimeWritePrivilegedOnly'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeWritePrivilegedOnly.true"
                                                      path="overtimeWritePrivilegedOnly" value="true"
                                                      data-test-id="setting-overtime-write-privileged-only-enabled" />
                                    <spring:message code="settings.overtime.overtimeWritePrivilegedOnly.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeWritePrivilegedOnly.false"
                                                      path="overtimeWritePrivilegedOnly" value="false"
                                                      data-test-id="setting-overtime-write-privileged-only-disabled" />
                                    <spring:message code="settings.overtime.overtimeWritePrivilegedOnly.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.overtimeActive.true">
                                <spring:message code='settings.overtime.overtimeReductionWithoutApplicationActive'/>:
                            </label>
                            <div class="col-md-8 radio">
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeReductionWithoutApplicationActive.true"
                                                      path="overtimeReductionWithoutApplicationActive" value="true"
                                                      data-test-id="setting-overtime-reduction-enabled" />
                                    <spring:message code="settings.overtime.overtimeReductionWithoutApplicationActive.true"/>
                                </label>
                                <label class="halves">
                                    <form:radiobutton id="overtimeSettings.overtimeReductionWithoutApplicationActive.false"
                                                      path="overtimeReductionWithoutApplicationActive" value="false"
                                                      data-test-id="setting-overtime-reduction-disabled" />
                                    <spring:message code="settings.overtime.overtimeReductionWithoutApplicationActive.false"/>
                                </label>
                            </div>
                        </div>

                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.maximumOvertime">
                                <spring:message code="settings.overtime.maximum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="overtimeSettings.maximumOvertime"
                                            path="maximumOvertime" class="form-control"
                                            cssErrorClass="form-control error"
                                            type="number" step="1"/>
                                <uv:error-text>
                                    <form:errors path="maximumOvertime" />
                                </uv:error-text>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.minimumOvertime">
                                <spring:message code="settings.overtime.minimum"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="overtimeSettings.minimumOvertime"
                                            path="minimumOvertime" class="form-control"
                                            cssErrorClass="form-control error"
                                            type="number" step="1"/>
                                <uv:error-text>
                                    <form:errors path="minimumOvertime" />
                                </uv:error-text>
                            </div>
                        </div>
                        <div class="form-group is-required">
                            <label class="control-label col-md-4" for="overtimeSettings.minimumOvertimeReduction">
                                <spring:message code="settings.overtime.minimumOvertimeReduction"/>:
                            </label>
                            <div class="col-md-8">
                                <form:input id="overtimeSettings.minimumOvertimeReduction"
                                            path="minimumOvertimeReduction" class="form-control"
                                            cssErrorClass="form-control error"
                                            type="number" step="1" />
                                <uv:error-text>
                                    <form:errors path="minimumOvertimeReduction" />
                                </uv:error-text>
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
