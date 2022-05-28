<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>

<uv:section-heading>
    <h2>
        <spring:message code='settings.absenceTypes.title'/>
    </h2>
</uv:section-heading>

<div class="tw-flex tw-flex-col xl:tw-flex-row xl:tw-flex-row-reverse">
    <div class="help-block tw-flex tw-flex-auto tw-justify-left tw-items-start md:tw-flex-none xl:tw-w-1/3 xl:tw-ml-3 xl:tw-pl-5 tw-pt-2 md:tw-pt-0 tw-text-sm">
        <div>
            <icon:information-circle className="tw-w-4 tw-h-4 tw-mr-1" solid="true"/>
        </div>
        <div class="tw-flex tw-flex-col">
            <p>
                <spring:message code="settings.absenceTypes.help.1"/>
                <a class="tw-inline-flex tw-items-center" target="_blank" rel="noopener" href="https://urlaubsverwaltung.cloud/hilfe/abwesenheiten/#welche-abwesenheitsarten-gibt-es">
                    <spring:message code="settings.absenceTypes.help.2"/>
                    <icon:external-link className="tw-ml-1 tw-h-4 tw-w-4" />
                </a>
            </p>
            <p>
                <spring:message code="settings.absenceTypes.description.1"/>
                <a class="tw-flex tw-items-center" href="mailto:info@urlaubsverwaltung.cloud?subject=Weitere%20Abwesenheitsart">
                    <icon:mail className="tw-mr-1 tw-h-4 tw-w-4" />
                    <spring:message code="settings.absenceTypes.description.2"/>
                </a>
            </p>
        </div>
    </div>
    <table id="absence-type-table" class="lg:tw-flex-1 absence-type-settings-table">
        <thead>
        <tr>
            <th scope="col">
                <spring:message code='settings.absenceTypes.table.head.state' />
            </th>
            <th scope="col">
                <spring:message code='settings.absenceTypes.table.head.type' />
            </th>
            <th scope="col">
                <spring:message code='settings.absenceTypes.table.head.category' />
            </th>
            <th scope="col" class="sm:tw-text-right sm:tw-relative">
                <span class="th-cell-overflow-text sm:tw-absolute sm:tw-top-1 sm:tw-right-4 tw-whitespace-nowrap">
                    <spring:message code='settings.absenceTypes.table.head.approval' />
                </span>
            </th>
            <th scope="col">
                <spring:message code='settings.absenceTypes.table.head.color' />
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${settings.absenceTypeSettings.items}" var="absenceType" varStatus="loop">
            <tr data-enabled="${absenceType.active}">
                <td data-col-status data-th-text="<spring:message code='settings.absenceTypes.table.head.state' />">
                    <span class="checkbox-switch">
                        <form:hidden path="absenceTypeSettings.items[${loop.index}].id" value="${absenceType.id}" />
                        <form:checkbox path="absenceTypeSettings.items[${loop.index}].active" id="absenceType-active-${loop.index}" />
                        <label for="absenceType-active-${loop.index}" class="tw-sr-only">
                            <spring:message code="settings.absenceTypes.action.state.label" />
                        </label>
                    </span>
                </td>
                <td data-th-text="<spring:message code='settings.absenceTypes.table.head.type' />">
                    <spring:message code="${absenceType.messageKey}" />
                </td>
                <td data-th-text="<spring:message code='settings.absenceTypes.table.head.category' />">
                    <spring:message code="${absenceType.category}" />
                </td>
                <td
                    data-th-text="<spring:message code='settings.absenceTypes.table.head.approval' />"
                    class="sm:tw-text-right"
                >
                    <form:checkbox
                        path="absenceTypeSettings.items[${loop.index}].requiresApproval"
                        id="absenceType-approval-${loop.index}"
                        cssClass="absence-type-approval-checkbox"
                    />
                    <label for="absenceType-approval-${loop.index}" class="tw-sr-only">
                        <spring:message code="settings.absenceTypes.action.approve.label" />
                    </label>
                </td>
                <td data-th-text="<spring:message code='settings.absenceTypes.table.head.color' />">
                    <label for="color-${loop.index}" class="tw-sr-only">
                        <spring:message code="settings.absenceTypes.action.color.label" />
                    </label>

                    <div is="uv-color-picker">
                        <label
                            id="color-picker-label-${loop.index}"
                            for="color-picker-${loop.index}"
                            class="color-picker-button"
                        >
                            <span
                                class="color-picker-button-color"
                                style="background-color:var(--absence-color-${absenceType.color})"
                            ></span>
                            <span class="tw-sr-only">
                                <spring:message code="settings.absenceTypes.action.color.label" />
                            </span>
                        </label>
                        <input type="checkbox" id="color-picker-${loop.index}">
                        <ul id="color-popup-${loop.index}" class="color-picker-dialog tw-list-none">
                            <c:forEach var="selectableColor" varStatus="selectableColorLoop" items="${settings.absenceTypeSettings.colors}">
                                <li class="color-picker-option" style="background-color:var(--absence-color-${selectableColor});">
                                    <label for="color-${loop.index}-radio-${selectableColorLoop.index}">
                                        <input
                                            <c:if test="${absenceType.color == selectableColor}">checked</c:if>
                                            value="${selectableColor}"
                                            id="color-${loop.index}-radio-${selectableColorLoop.index}"
                                            name="absenceTypeSettings.items[${loop.index}].color"
                                            type="radio"
                                        >
                                    </label>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
