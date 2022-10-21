<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<spring:url var="URL_PREFIX" value="/web"/>

<div class="navigation print:tw-hidden">
    <nav class="tw-relative">
        <input id="menu-toggle-checkbox" type="checkbox" class="tw-hidden">
        <label for="menu-toggle-checkbox" class="tw-flex tw-items-center tw-m-0 tw-px-4 tw-py-4 lg:tw-hidden tw-cursor-pointer">
            <span class="tw-inline-block">
                <span class="hamburger-bar"></span>
                <span class="hamburger-bar"></span>
                <span class="hamburger-bar"></span>
                <span class="hamburger-bar"></span>
                <span class="hamburger-bar"></span>
            </span>
        </label>
        <div class="navigation-inner tw-flex lg:tw-justify-between tw-px-6 lg:tw-px-8">
            <div class="tw-flex-1 tw-hidden 2xl:tw-flex xl:tw-items-center">
                <a
                    href="/"
                    class="tw-font-logo tw-text-xl tw-font-medium tw-mr-8 md:tw-mr-16 tw-text-zinc-900 dark:tw-text-zinc-100 tw-no-underline"
                    th:text="#{nav.urlaubsverwaltung.title}"
                >
                    Urlaubsverwaltung
                </a>
            </div>
            <div class="tw-w-full tw-max-w-6xl tw-flex navigation-items tw-max-h-0 lg:tw-max-h-full">
                <ul class="navigation-list tw-list-none tw-m-0 tw-px-0 tw-py-8 lg:tw-py-3 lg:tw-px-2 xl:tw-px-0">
                    <li>
                        <a
                            href="${URL_PREFIX}/overview"
                            id="home-link"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            title="<spring:message code="nav.home.title"/>"
                        >
                            <icon:home className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.home.title"/>
                            </span>
                        </a>
                    </li>
                    <li>
                        <a
                            href="${URL_PREFIX}/application/new"
                            id="application-new-link"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            title="<spring:message code="nav.apply.title"/>"
                        >
                            <icon:plus-circle className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.apply.title"/>
                            </span>
                        </a>
                    </li>
                    <li>
                        <a
                            href="${URL_PREFIX}/application"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            tile="<spring:message code="nav.vacation.title"/>"
                        >
                            <icon:calendar className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.vacation.title"/>
                            </span>
                        </a>
                    </li>
                    <c:if test="${navigationSickNoteStatisticsAccess}">
                    <li>
                        <a
                            href="${URL_PREFIX}/sicknote"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2" data-test-id="navigation-sick-notes-link"
                            title="<spring:message code="nav.sicknote.title"/>"
                        >
                            <icon:medkit className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.sicknote.title"/>
                            </span>
                        </a>
                    </li>
                    </c:if>
                    <c:if test="${navigationPersonListAccess}">
                    <li>
                        <a
                            href="${URL_PREFIX}/person?active=true"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            title="<spring:message code="nav.person.title"/>"
                        >
                            <icon:user className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.person.title"/>
                            </span>
                        </a>
                    </li>
                    </c:if>
                    <c:if test="${navigationDepartmentAccess}">
                    <li>
                        <a
                            href="${URL_PREFIX}/department"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            title="<spring:message code="nav.department.title"/>"
                        >
                            <icon:user-group className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.department.title"/>
                            </span>
                        </a>
                    </li>
                    </c:if>
                    <c:if test="${navigationSettingsAccess}">
                    <li>
                        <a
                            href="${URL_PREFIX}/settings"
                            class="group tw-no-underline tw-flex tw-items-center tw-text-zinc-900 tw-overflow-hidden hover:tw-text-blue-400 focus:tw-text-blue-400 tw-transition-colors tw-text-2xl tw-font-medium tw-no-underline tw-space-x-5 dark:tw-text-zinc-100 dark:hover:tw-text-zinc-400 dark:focus:tw-text-zinc-400 lg:tw-text-lg lg:tw-font-normal lg:tw-space-x-2 xl:tw-space-x-2"
                            data-test-id="navigation-settings-link"
                            <spring:message code="nav.settings.title"/>
                        >
                            <icon:cog className="tw-shrink-0 tw-text-zinc-900 tw-text-opacity-50 tw-transition-colors dark:tw-text-zinc-100 dark:tw-text-opacity-100 tw-w-8 tw-h-8 lg:tw-w-5 lg:tw-h-5 group-hover:tw-text-blue-400 group-hover:dark:tw-text-zinc-400 group-focus:tw-text-blue-400 group-focus:dark:tw-text-zinc-400" />
                            <span class="tw-overflow-hidden tw-text-ellipsis tw-whitespace-nowrap" style="min-width: 0;">
                                <spring:message code="nav.settings.title"/>
                            </span>
                        </a>
                    </li>
                    </c:if>
                </ul>
            </div>
            <div class="tw-absolute tw-right-0 tw-top-0 lg:tw-static lg:tw-flex-1 tw-flex tw-justify-end">
                <div class="tw-px-6 lg:tw-px-0 tw-py-3 lg:tw-py-2 tw-flex tw-space-x-2">
                    <c:choose>
                        <c:when test="${navigationRequestPopupEnabled}">
                            <div class="tw-relative">
                                <a
                                    href="#add-something-new-menu"
                                    id="add-something-new"
                                    class="nav-popup-menu-button tw-flex tw-items-center tw-no-underline tw-px-1.5 tw-outline-none"
                                    data-test-id="add-something-new"
                                    tabindex="-1"
                                >
                                    <icon:plus className="nav-popup-menu-button_icon tw-w-8 tw-h-8 lg:tw-w-9 lg:tw-h-9" />
                                    <span class="tw-sr-only">
                                        <spring:message code="nav.add.button.text" />
                                    </span>
                                    <span class="dropdown-caret"></span>
                                </a>
                                <div
                                    id="add-something-new-menu"
                                    class="nav-popup-menu"
                                    data-test-id="add-something-new-popupmenu"
                                >
                                    <div class="nav-popup-menu-inner">
                                        <ul class="tw-list-none tw-m-0 tw-p-0 tw-max-w-xs">
                                            <li>
                                                <a
                                                    href="${URL_PREFIX}/application/new"
                                                    class="nav-popup-menu_link"
                                                    data-test-id="quick-add-new-application"
                                                >
                                                    <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                        <icon:calendar className="tw-h-6 tw-w-6" />
                                                    </span>
                                                    <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden">
                                                        <spring:message code="nav.add.vacation" />
                                                    </span>
                                                </a>
                                            </li>
                                            <c:if test="${navigationSickNoteAddAccess}">
                                                <li>
                                                    <a
                                                        href="${URL_PREFIX}/sicknote/new"
                                                        class="nav-popup-menu_link"
                                                        data-test-id="quick-add-new-sicknote"
                                                    >
                                                        <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                            <icon:medkit className="tw-h-6 tw-w-6" />
                                                        </span>
                                                        <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden">
                                                            <spring:message code="nav.add.sicknote" />
                                                        </span>
                                                    </a>
                                                </li>
                                            </c:if>
                                            <c:if test="${navigationOvertimeItemEnabled}">
                                                <li>
                                                    <a
                                                        href="${URL_PREFIX}/overtime/new"
                                                        class="nav-popup-menu_link"
                                                        data-test-id="quick-add-new-overtime"
                                                    >
                                                        <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                            <icon:briefcase className="tw-h-6 tw-w-6" />
                                                        </span>
                                                        <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden">
                                                            <spring:message code="nav.add.overtime" />
                                                        </span>
                                                    </a>
                                                </li>
                                            </c:if>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <a
                                href="${URL_PREFIX}/application/new"
                                class="tw-flex tw-items-center"
                                data-test-id="new-application"
                            >
                                <icon:plus className="nav-popup-menu-button_icon tw-w-8 tw-h-8 lg:tw-w-9 lg:tw-h-9" />
                                <span class="tw-sr-only">
                                    <spring:message code="nav.add.vacation" />
                                </span>
                            </a>
                        </c:otherwise>
                    </c:choose>
                    <div class="tw-relative">
                        <a
                            href="#avatar-menu"
                            class="nav-popup-menu-button tw-flex tw-items-center tw-gap-1 tw-no-underline tw-outline-none"
                            id="avatar-link"
                            data-test-id="avatar"
                            tabindex="-1"
                        >
                            <img
                                src="<c:out value='${menuGravatarUrl}?d=404'/>"
                                alt=""
                                class="gravatar tw-rounded-full nav-popup-menu-button_icon tw-w-8 tw-h-8 lg:tw-w-9 lg:tw-h-9"
                                width="52px"
                                height="52px"
                                onerror="this.onerror=null;this.src='<c:out value="${URL_PREFIX}/avatar?name=${userFirstName + ' ' + userLastName}"/>'"
                            />
                            <span class="dropdown-caret tw-translate-x-px"></span>
                        </a>
                        <div
                            id="avatar-menu"
                            class="nav-popup-menu avatar-menu dark:tw-border-zinc-800"
                            data-test-id="avatar-popupmenu"
                        >
                            <div class="nav-popup-menu-inner">
                                <ul class="tw-list-none tw-m-0 tw-p-0 tw-max-w-xs">
                                    <li class="tw-mb-1">
                                        <a
                                            href="${URL_PREFIX}/person/${userId}/overview"
                                            class="tw-bg-gradient-to-br tw-from-blue-50 tw-via-gray-100 tw-to-blue-100 dark:tw-from-sky-800 dark:tw-via-slate-800 dark:tw-to-sky-900 tw-bg-blue-50 tw-px-6 tw-py-3 tw-rounded-t-2xl tw-flex tw-items-center tw-gap-4 hover:tw-no-underline focus:tw-no-underline"
                                        >
                                            <img
                                                src="<c:out value='${menuGravatarUrl}?d=404&s=128'/>"
                                                alt=""
                                                class="gravatar tw-rounded-full"
                                                width="64px"
                                                height="64px"
                                                onerror="this.onerror=null;this.src='<c:out value="${URL_PREFIX}/avatar?name=${userFirstName}"/>'"
                                            />
                                            <p class="tw-whitespace-nowrap tw-overflow-hidden tw-text-ellipsis">
                                                <span class="tw-text-sm tw-block tw-text-gray-600 dark:tw-text-zinc-100">
                                                    <spring:message code="nav.avatar-menu.signed-in-as" />
                                                </span>
                                                <span class="tw-text-lg tw-leading-none tw-text-gray-700 hover:tw-text-black focus:tw-text-black dark:tw-text-zinc-200">
                                                    ${userFirstName} ${userLastName}
                                                </span>
                                            </p>
                                        </a>
                                    </li>
                                    <li class="tw-mb-1">
                                        <a
                                            href="${menuHelpUrl}"
                                            class="nav-popup-menu_link"
                                            target="_blank"
                                            rel="noopener"
                                        >
                                            <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                <icon:question-mark-circle className="tw-h-6 tw-w-6" />
                                            </span>
                                            <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden" style="min-width: 0;">
                                                <spring:message code="nav.help.title" />
                                            </span>
                                            <icon:external-link className="tw-ml-1.5 tw-h-4 tw-w-4" />
                                        </a>
                                    </li>
                                    <li class="tw-mb-1">
                                        <a
                                            href="${URL_PREFIX}/person/${userId}"
                                            class="nav-popup-menu_link"
                                        >
                                            <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                <icon:user-circle className="tw-h-6 tw-w-6" />
                                            </span>
                                            <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden" style="min-width: 0;">
                                                <spring:message code="nav.account.title" />
                                            </span>
                                        </a>
                                    </li>
                                    <li class="tw-mb-1">
                                        <a
                                            href="${URL_PREFIX}/person/${userId}/settings"
                                            class="nav-popup-menu_link"
                                        >
                                            <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                <icon:adjustments className="tw-h-6 tw-w-6" />
                                            </span>
                                            <span class="tw-ml-4 tw-whitespace-nowrap tw-text-ellipsis tw-overflow-hidden" style="min-width: 0;">
                                                <spring:message code="nav.user-settings.title" />
                                            </span>
                                        </a>
                                    </li>
                                    <li role="separator"></li>
                                    <li>
                                        <form:form action="/logout" method="POST" cssClass="tw-ml-auto tw-w-full">
                                            <button
                                                type="submit"
                                                class="nav-popup-menu_link"
                                                data-test-id="logout"
                                            >
                                                <span class="tw-flex tw-items-center">
                                                    <span class="tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-ml-2.5">
                                                        <icon:logout className="tw-w-6 tw-h-6" />
                                                    </span>
                                                    <span class="tw-ml-4">
                                                        <spring:message code="nav.signout.title"/>
                                                    </span>
                                                </span>
                                            </button>
                                        </form:form>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="navigation-content-spacer tw-flex tw-justify-between tw-w-full lg:tw-hidden">
            <div class="navigation-content-spacer--left tw-h-4 tw-w-4"></div>
            <div class="navigation-content-spacer--right tw-h-4 tw-w-4"></div>
        </div>
    </nav>
</div>
