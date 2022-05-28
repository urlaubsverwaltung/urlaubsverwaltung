<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code='settings.specialleave.title'/>
    </h2>
</uv:section-heading>

<div class="tw-flex tw-flex-col xl:tw-flex-row xl:tw-flex-row-reverse">
    <div class="help-block tw-flex tw-flex-auto tw-justify-left tw-items-start xl:tw-flex-none xl:tw-w-1/3 xl:tw-ml-3 xl:tw-pl-5 tw-pt-2 xl:tw-pt-0 tw-text-sm">
        <div>
            <icon:information-circle className="tw-w-4 tw-h-4" solid="true" />
        </div>
        <div class="tw-flex tw-flex-col">
            <p>
                <spring:message code="settings.specialleave.help"/>
            </p>
            <p>
                <spring:message code="settings.specialleave.description.1"/>
                <a class="tw-flex tw-items-center" href="mailto:info@urlaubsverwaltung.cloud?subject=Missing%20special%20leaves">
                    <icon:mail className="tw-mr-1 tw-h-4 tw-w-4" />
                    <spring:message code="settings.specialleave.description.2"/>
                </a>
            </p>
        </div>
    </div>
    <table id="special-leave-table" class="lg:tw-flex-1 absence-type-settings-table">
        <caption class="tw-sr-only">
            <spring:message code='settings.specialleave.title'/>
        </caption>
        <thead>
        <tr>
            <th scope="col">
                <spring:message code='settings.specialleave.table.head.state' />
            </th>
            <th scope="col">
                <spring:message code='settings.specialleave.table.head.type' />
            </th>
            <th scope="col">
                <spring:message code='settings.specialleave.table.head.days' />
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${settings.specialLeaveSettings.specialLeaveSettingsItems}" var="specialLeaveSettingsItem" varStatus="loop">
            <tr data-enabled="${specialLeaveSettingsItem.active}">
                <td data-col-status data-th-text="<spring:message code='settings.specialleave.table.head.state' />">
                    <span class="checkbox-switch">
                        <form:hidden path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].id" value="${specialLeaveSettingsItem.id}" />
                        <form:checkbox path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].active" id="specialLeave-active-${loop.index}" />
                        <label for="specialLeave-active-${loop.index}" class="tw-sr-only">
                            <spring:message code="settings.specialleave.action.state.label" />
                        </label>
                    </span>
                </td>
                <td data-th-text="<spring:message code='settings.specialleave.table.head.type' />">
                    <span class="tw-w-1/2 md:tw-w-full">
                        <spring:message code="${specialLeaveSettingsItem.messageKey}" />
                    </span>
                </td>
                <td data-th-text="<spring:message code='settings.specialleave.table.head.days' />">
                    <form:input id="specialLeave-days-${loop.index}" cssClass="form-control tw-w-1/2 sm:tw-w-full"
                                path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].days" class="form-control"
                                cssErrorClass="form-control error"
                                type="number" step="1" min="0"/>
                    <uv:error-text>
                        <form:errors path="specialLeaveSettings.specialLeaveSettingsItems[${loop.index}].days" />
                    </uv:error-text>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
