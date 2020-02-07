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

<div class="content container">
    <header>
        <h1 class="mb-8 text-2xl text-medium">
            <spring:message code="calendar.share.title"/>
        </h1>
    </header>
    <main>
        <form:form method="POST" action="${URL_PREFIX}/persons/${privateCalendarShare.personId}/calendar/share/me"
                   modelAttribute="privateCalendarShare">
            <fieldset class="mb-4">
                <legend class="text-xl">
                    <spring:message code="calendar.share.me.title"/>
                </legend>
                <c:choose>
                    <c:when test="${empty privateCalendarShare.calendarUrl}">
                        <div class="max-w-3xl">
                            <p class="mb-8 text-base">
                                <spring:message code="calendar.share.me.paragraph.status"/>
                            </p>
                            <p class="mb-2 text-base">
                                <spring:message code="calendar.share.me.paragraph.info"/>
                            </p>
                            <p class="mb-4 text-base">
                                <spring:message code="calendar.share.me.paragraph.info.reset"/>
                            </p>
                            <button type="submit" class="btn btn-primary">
                                <spring:message code="calendar.share.me.form.submit.text"/>
                            </button>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="max-w-4xl">
                            <p class="mb-8 text-base">
                                <spring:message code="calendar.share.me.isshared.paragraph.status"/>
                            </p>
                            <p class="mb-2 text-base">
                                <spring:message code="calendar.share.me.isshared.paragraph.info"/>
                            </p>
                            <div
                                is="uv-copy-to-clipboard-input"
                                class="flex flex-row mb-8 border border-gray-300 focus-within:shadow-outline"
                                data-message-button-title="<spring:message code="calendar.share.me.button.clipboard.tooltip" />"
                                data-message-copy-success-info="<spring:message code="calendar.share.me.button.clipboard.done" />"
                            >
                                <input type="text" value="${privateCalendarShare.calendarUrl}"
                                       class="px-3 py-2 text-base flex-1 border-0 outline-none" readonly/>
                            </div>
                            <p class="mb-8 text-base">
                                <spring:message code="calendar.share.me.reset.paragraph"/>
                            </p>
                            <div class="flex flex-col sm:flex-row">
                                <button type="submit" class="btn btn-default mb-4 sm:mb-0">
                                    <spring:message code="calendar.share.me.reset.form.submit.text"/>
                                </button>
                                <button type="submit" name="unlink" class="btn btn-default sm:mb-0">
                                    <spring:message code="calendar.share.me.unlink.form.submit.text"/>
                                </button>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </fieldset>
        </form:form>

        <div>
            <fieldset class="mb-4">
                <legend class="text-xl">
                    <spring:message code="calendar.share.department.title"/>
                </legend>
                <div>
                    <ul class="nav nav-tabs">
                        <c:forEach items="${departmentCalendars}" var="departmentCal" varStatus="loop">
                            <li role="presentation" class="${departmentCal.active ? 'active' : ''}">
                                <a href="#departmentcal-${departmentCal.departmentId}" aria-controls="departmentcal-${departmentCal.departmentId}" role="tab" data-toggle="tab">
                                    ${departmentCal.departmentName}
                                </a>
                            </li>
                        </c:forEach>
                    </ul>
                    <div class="tab-content">
                        <c:forEach items="${departmentCalendars}" var="departmentCal" varStatus="loop">
                            <div role="tabpanel" class="tab-pane${departmentCal.active ? ' active' : ''}" id="departmentcal-${departmentCal.departmentId}">
                                <form:form method="POST" action="${URL_PREFIX}/persons/${privateCalendarShare.personId}/calendar/share/departments/${departmentCal.departmentId}" id="department-calendar-form-${loop.index}">
                                    <c:choose>
                                    <c:when test="${empty departmentCal.calendarUrl}">
                                        <div class="max-w-3xl">
                                            <p class="mb-8 text-base">
                                                <spring:message code="calendar.share.department.paragraph.status" arguments="${departmentCal.departmentName}"/>
                                            </p>
                                            <p class="mb-2 text-base">
                                                <spring:message code="calendar.share.department.paragraph.info"/>
                                            </p>
                                            <p class="mb-4 text-base">
                                                <spring:message code="calendar.share.department.paragraph.info.reset"/>
                                            </p>
                                            <button type="submit" class="btn btn-primary">
                                                <spring:message code="calendar.share.department.form.submit.text" arguments="${departmentCal.departmentName}" />
                                            </button>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="max-w-4xl">
                                            <p class="mb-8 text-base">
                                                <spring:message code="calendar.share.department.isshared.paragraph.status"/>
                                            </p>
                                            <p class="mb-2 text-base">
                                                <spring:message code="calendar.share.department.isshared.paragraph.info"/>
                                            </p>
                                            <div
                                                is="uv-copy-to-clipboard-input"
                                                class="flex flex-row mb-8 border border-gray-300 focus-within:shadow-outline"
                                                data-message-button-title="<spring:message code="calendar.share.department.button.clipboard.tooltip" />"
                                                data-message-copy-success-info="<spring:message code="calendar.share.department.button.clipboard.done" />"
                                            >
                                                <input type="text" value="${departmentCal.calendarUrl}"
                                                       class="px-3 py-2 text-base flex-1 border-0 outline-none" readonly/>
                                            </div>
                                            <p class="mb-8 text-base">
                                                <spring:message code="calendar.share.department.reset.paragraph"/>
                                            </p>
                                            <div class="flex flex-col sm:flex-row">
                                                <button type="submit" class="btn btn-default mb-4 sm:mb-0">
                                                    <spring:message code="calendar.share.department.reset.form.submit.text"/>
                                                </button>
                                                <button type="submit" name="unlink" class="btn btn-default sm:mb-0">
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

            </fieldset>
        </div>
    </main>
</div>

</body>
</html>
