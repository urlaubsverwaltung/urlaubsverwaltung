<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<spring:url var="URL_PREFIX" value="/web"/>

<!DOCTYPE html>
<html lang="${language}">
<head>
    <title>
        <spring:message code="calendar.share.header.title"/>
    </title>
    <uv:custom-head/>
    <script defer src="<asset:url value="copy_to_clipboard_input.js" />"></script>
    <script defer src="<asset:url value="tabs.js" />"></script>
</head>
<body>

<uv:menu/>

<div class="content container tw-mb-20">
    <header>
        <h1 class="tw-mb-8 tw-text-2xl tw-text-medium">
            <spring:message code="calendar.share.title"/>
        </h1>
    </header>
    <main>

        <p class="tw-mb-8">
            <spring:message code="calendar.share.info"/>
            <a href="https://github.com/synyx/urlaubsverwaltung/wiki/Fragen-zur-iCal-Kalenderfreigabe">
                <spring:message code="calendar.share.info.wiki"/>
            </a>
        </p>

        <div class="tw-mb-8">
            <uv:privacy-box>
                <p class="tw-space-y-4">
                    <spring:message code="calendar.share.privacy-info.paragraph"/>
                </p>
                <p>
                    <spring:message code="calendar.share.privacy-info.reset"/>
                </p>
            </uv:privacy-box>
        </div>

        <form:form method="POST" action="${URL_PREFIX}/calendars/share/persons/${privateCalendarShare.personId}/me"
                   modelAttribute="privateCalendarShare" cssClass="tw-mb-8">
            <div class="tw-mb-4">
                <uv:section-heading>
                    <h2 class="tw-text-xl">
                        <spring:message code="calendar.share.me.title"/>
                    </h2>
                </uv:section-heading>
                <c:choose>
                    <c:when test="${empty privateCalendarShare.calendarUrl}">
                        <div class="tw-max-w-3xl">
                            <p class="tw-mb-8 tw-text-base">
                                <spring:message code="calendar.share.me.paragraph.status"/>
                            </p>
                            <p>
                                <spring:message code="calendar.share.range.paragraph"/>
                            </p>
                            <div class="tw-mb-4">
                                <c:forEach items="${calendarPeriods}" varStatus="loop">
                                    <div>
                                        <label for="personal-calendar-period-${loop.index}" class="tw-flex tw-items-center tw-space-x-1">
                                            <input
                                                id="personal-calendar-period-${loop.index}"
                                                type="radio"
                                                name="calendarPeriod"
                                                value="${loop.current}"
                                                ${privateCalendarShare.calendarPeriod == loop.current ? 'checked="checked"' : ''}
                                                class="tw-m-0"
                                            >
                                            <span>
                                                <spring:message code="calendar.share.range.${loop.current}"/>
                                            </span>
                                        </label>
                                    </div>
                                </c:forEach>
                            </div>
                            <button type="submit" class="btn btn-primary">
                                <spring:message code="calendar.share.me.form.submit.text"/>
                            </button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="tw-max-w-4xl">
                            <p class="tw-mb-8 tw-text-base">
                                <spring:message var="calendarPeriodText" code="calendar.share.range.${privateCalendarShare.calendarPeriod}" />
                                <spring:message code="calendar.share.me.isshared.paragraph.status" arguments="${calendarPeriodText}"/>
                            </p>
                            <p class="tw-mb-2 tw-text-base">
                                <spring:message code="calendar.share.me.isshared.paragraph.info"/>
                            </p>
                            <div
                                is="uv-copy-to-clipboard-input"
                                class="tw-flex tw-flex-row tw-mb-8 tw-border tw-border-gray-200 focus-within:tw-ring"
                                data-message-button-title="<spring:message code="calendar.share.me.button.clipboard.tooltip" />"
                            >
                                <input type="text" value="${privateCalendarShare.calendarUrl}"
                                       class="tw-px-3 tw-py-2 tw-text-base tw-flex-1 tw-border-0 tw-outline-none" readonly/>
                            </div>
                            <p class="tw-mb-8 tw-text-base">
                                <spring:message code="calendar.share.me.reset.paragraph"/>
                            </p>
                            <div class="tw-flex tw-flex-col sm:tw-flex-row">
                                <button type="submit" class="btn btn-default tw-mb-4 sm:tw-mb-0">
                                    <spring:message code="calendar.share.me.reset.form.submit.text"/>
                                </button>
                                <button type="submit" name="unlink" class="btn btn-default sm:tw-mb-0">
                                    <spring:message code="calendar.share.me.unlink.form.submit.text"/>
                                </button>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </form:form>

        <c:if test="${departmentCalendars.size() > 0}">
            <div class="tw-mb-8">
                <uv:section-heading>
                    <h2 class="tw-text-xl">
                        <spring:message code="calendar.share.department.title"/>
                    </h2>
                </uv:section-heading>
                <c:if test="${departmentCalendars.size() > 1}">
                    <ul class="nav nav-tabs">
                        <c:forEach items="${departmentCalendars}" var="departmentCal" varStatus="loop">
                            <li role="presentation" class="${departmentCal.active ? 'active' : ''}">
                                <a href="#departmentcal-${departmentCal.departmentId}" aria-controls="departmentcal-${departmentCal.departmentId}" role="tab" data-toggle="tab">
                                    ${departmentCal.departmentName}
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                    </c:if>
                <c:set var="tabContentCssClass" value="${departmentCalendars.size() > 1 ? 'tab-content' : ''}" />
                <div class="${tabContentCssClass}">
                    <c:forEach items="${departmentCalendars}" var="departmentCal" varStatus="loop">
                        <div role="tabpanel" class="tab-pane${departmentCal.active ? ' active' : ''}" id="departmentcal-${departmentCal.departmentId}">
                            <form:form
                                method="POST"
                                action="${URL_PREFIX}/calendars/share/persons/${privateCalendarShare.personId}/departments/${departmentCal.departmentId}"
                                id="department-calendar-form-${loop.index}"
                            >
                                <c:choose>
                                <c:when test="${empty departmentCal.calendarUrl}">
                                    <div class="tw-max-w-3xl">
                                        <p class="tw-mb-8 tw-text-base">
                                            <spring:message code="calendar.share.department.paragraph.status" arguments="${departmentCal.departmentName}"/>
                                        </p>
                                        <p>
                                            <spring:message code="calendar.share.range.paragraph"/>
                                        </p>
                                        <div class="tw-mb-4">
                                            <c:forEach items="${calendarPeriods}" varStatus="periodLoop">
                                                <div>
                                                    <label
                                                        for="department-${departmentCal.departmentId}-calendar-period-${periodLoop.index}"
                                                        class="tw-flex tw-items-center tw-space-x-1"
                                                    >
                                                        <input
                                                            id="department-${departmentCal.departmentId}-calendar-period-${periodLoop.index}"
                                                            type="radio"
                                                            name="calendarPeriod"
                                                            value="${periodLoop.current}"
                                                            ${departmentCal.calendarPeriod == periodLoop.current ? 'checked="checked"' : ''}
                                                            class="tw-m-0"
                                                        >
                                                        <span>
                                                            <spring:message code="calendar.share.range.${periodLoop.current}"/>
                                                        </span>
                                                    </label>
                                                </div>
                                            </c:forEach>
                                        </div>
                                        <button type="submit" class="btn btn-primary">
                                            <spring:message code="calendar.share.department.form.submit.text" arguments="${departmentCal.departmentName}" />
                                        </button>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="tw-max-w-4xl">
                                        <p class="tw-mb-8 tw-text-base">
                                            <spring:message var="calendarPeriodText" code="calendar.share.range.${departmentCal.calendarPeriod}" />
                                            <spring:message
                                                code="calendar.share.department.isshared.paragraph.status"
                                                arguments="${departmentCal.departmentName}:::${calendarPeriodText}"
                                                argumentSeparator=":::"
                                            />
                                        </p>
                                        <p class="tw-mb-2 tw-text-base">
                                            <spring:message code="calendar.share.department.isshared.paragraph.info"/>
                                        </p>
                                        <div
                                            is="uv-copy-to-clipboard-input"
                                            class="tw-flex tw-flex-row tw-mb-8 tw-border tw-border-gray-200 focus-within:tw-ring"
                                            data-message-button-title="<spring:message code="calendar.share.department.button.clipboard.tooltip" />"
                                        >
                                            <input type="text" value="${departmentCal.calendarUrl}"
                                                   class="tw-px-3 tw-py-2 tw-text-base tw-flex-1 tw-border-0 tw-outline-none" readonly/>
                                        </div>
                                        <p class="tw-mb-8 tw-text-base">
                                            <spring:message code="calendar.share.department.reset.paragraph"/>
                                        </p>
                                        <div class="tw-flex tw-flex-col sm:tw-flex-row">
                                            <button type="submit" class="btn btn-default tw-mb-4 sm:tw-mb-0">
                                                <spring:message code="calendar.share.department.reset.form.submit.text"/>
                                            </button>
                                            <button type="submit" name="unlink" class="btn btn-default sm:tw-mb-0">
                                                <spring:message code="calendar.share.department.unlink.form.submit.text"/>
                                            </button>
                                        </div>
                                    </div>
                                </c:otherwise>
                                </c:choose>
                            </form:form>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

        <c:if test="${companyCalendarAccessible != null || companyCalendarShare != null}">
            <div>
                <uv:section-heading>
                    <h2 class="tw-text-xl">
                        <spring:message code="calendar.share.company.title"/>
                    </h2>
            </uv:section-heading>
                <c:if test="${companyCalendarAccessible != null}">
                    <form:form method="POST" action="${URL_PREFIX}/calendars/share/persons/${personId}/company/accessible" modelAttribute="companyCalendarAccessible">
                        <form:hidden path="accessible" value="${!companyCalendarAccessible.accessible}" />
                        <div class="tw-max-w-3xl tw-mb-12">
                            <p class="tw-mb-4 tw-text-base">
                            <c:choose>
                                <c:when test="${companyCalendarAccessible.accessible}">
                                    <spring:message code="calendar.share.company.accessible.enabled.paragraph"/>
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="calendar.share.company.accessible.disabled.paragraph"/>
                                </c:otherwise>
                            </c:choose>
                            </p>
                            <c:choose>
                            <c:when test="${companyCalendarAccessible.accessible}">
                                <button type="submit" class="btn btn-default">
                                    <spring:message code="calendar.share.company.accessible.disable.button.text"/>
                                </button>
                            </c:when>
                            <c:otherwise>
                                <button type="submit" class="btn btn-primary">
                                    <spring:message code="calendar.share.company.accessible.enable.button.text"/>
                                </button>
                            </c:otherwise>
                            </c:choose>
                        </div>
                    </form:form>
                </c:if>

                <c:if test="${companyCalendarShare != null}">
                    <form:form method="POST" action="${URL_PREFIX}/calendars/share/persons/${companyCalendarShare.personId}/company" modelAttribute="companyCalendarShare">
                        <c:choose>
                            <c:when test="${empty companyCalendarShare.calendarUrl}">
                                <div class="tw-max-w-3xl">
                                    <p class="tw-mb-8 tw-text-base">
                                        <spring:message code="calendar.share.company.paragraph.status"/>
                                    </p>
                                    <p>
                                        <spring:message code="calendar.share.range.paragraph"/>
                                    </p>
                                    <div class="tw-mb-4">
                                        <c:forEach items="${calendarPeriods}" varStatus="periodLoop">
                                            <div>
                                                <label
                                                    for="company-calendar-period-${periodLoop.index}"
                                                    class="tw-flex tw-items-center tw-space-x-1"
                                                >
                                                    <input
                                                        id="company-calendar-period-${periodLoop.index}"
                                                        type="radio"
                                                        name="calendarPeriod"
                                                        value="${periodLoop.current}"
                                                        ${companyCalendarShare.calendarPeriod == periodLoop.current ? 'checked="checked"' : ''}
                                                        class="tw-m-0"
                                                    >
                                                    <span>
                                                        <spring:message code="calendar.share.range.${periodLoop.current}"/>
                                                    </span>
                                                </label>
                                            </div>
                                        </c:forEach>
                                    </div>
                                    <button type="submit" class="btn btn-primary">
                                        <spring:message code="calendar.share.company.form.submit.text"/>
                                    </button>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="tw-max-w-4xl">
                                    <p class="tw-mb-8 tw-text-base">
                                        <spring:message var="calendarPeriodText" code="calendar.share.range.${companyCalendarShare.calendarPeriod}" />
                                        <spring:message code="calendar.share.company.isshared.paragraph.status" arguments="${calendarPeriodText}"/>
                                    </p>
                                    <p class="tw-mb-2 tw-text-base">
                                        <spring:message code="calendar.share.company.isshared.paragraph.info"/>
                                    </p>
                                    <div
                                        is="uv-copy-to-clipboard-input"
                                        class="tw-flex tw-flex-row tw-mb-8 tw-border tw-border-gray-200 focus-within:tw-ring"
                                        data-message-button-title="<spring:message code="calendar.share.company.button.clipboard.tooltip" />"
                                    >
                                        <input type="text" value="${companyCalendarShare.calendarUrl}"
                                               class="tw-px-3 tw-py-2 tw-text-base tw-flex-1 tw-border-0 tw-outline-none" readonly/>
                                    </div>
                                    <p class="tw-mb-8 tw-text-base">
                                        <spring:message code="calendar.share.company.reset.paragraph"/>
                                    </p>
                                    <div class="tw-flex tw-flex-col sm:tw-flex-row">
                                        <button type="submit" class="btn btn-default tw-mb-4 sm:tw-mb-0">
                                            <spring:message code="calendar.share.company.reset.form.submit.text"/>
                                        </button>
                                        <button type="submit" name="unlink" class="btn btn-default sm:tw-mb-0">
                                            <spring:message code="calendar.share.company.unlink.form.submit.text"/>
                                        </button>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </form:form>
                </c:if>
            </div>
        </c:if>
    </main>
</div>

</body>
</html>
