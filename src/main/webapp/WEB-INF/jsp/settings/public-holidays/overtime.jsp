<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

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
                                      path="overtimeSettings.overtimeActive" value="true"
                                      data-test-id="setting-overtime-enabled" />
                    <spring:message code="settings.overtime.overtimeActive.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="overtimeSettings.overtimeActive.false"
                                      path="overtimeSettings.overtimeActive" value="false"
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
                                      path="overtimeSettings.overtimeWritePrivilegedOnly" value="true"
                                      data-test-id="setting-overtime-write-privileged-only-enabled" />
                    <spring:message code="settings.overtime.overtimeWritePrivilegedOnly.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="overtimeSettings.overtimeWritePrivilegedOnly.false"
                                      path="overtimeSettings.overtimeWritePrivilegedOnly" value="false"
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
                                      path="overtimeSettings.overtimeReductionWithoutApplicationActive" value="true"
                                      data-test-id="setting-overtime-reduction-enabled" />
                    <spring:message code="settings.overtime.overtimeReductionWithoutApplicationActive.true"/>
                </label>
                <label class="halves">
                    <form:radiobutton id="overtimeSettings.overtimeReductionWithoutApplicationActive.false"
                                      path="overtimeSettings.overtimeReductionWithoutApplicationActive" value="false"
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
                            path="overtimeSettings.maximumOvertime"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="overtimeSettings.maximumOvertime" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="overtimeSettings.minimumOvertime">
                <spring:message code="settings.overtime.minimum"/>:
            </label>
            <div class="col-md-8">
                <form:input id="overtimeSettings.minimumOvertime"
                            path="overtimeSettings.minimumOvertime"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1"/>
                <uv:error-text>
                    <form:errors path="overtimeSettings.minimumOvertime" />
                </uv:error-text>
            </div>
        </div>
        <div class="form-group is-required">
            <label class="control-label col-md-4" for="overtimeSettings.minimumOvertimeReduction">
                <spring:message code="settings.overtime.minimumOvertimeReduction"/>:
            </label>
            <div class="col-md-8">
                <form:input id="overtimeSettings.minimumOvertimeReduction"
                            path="overtimeSettings.minimumOvertimeReduction"
                            class="form-control"
                            cssErrorClass="form-control error"
                            type="number" step="1" />
                <uv:error-text>
                    <form:errors path="overtimeSettings.minimumOvertimeReduction" />
                </uv:error-text>
            </div>
        </div>
    </div>
</div>
