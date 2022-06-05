<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}" class="tw-<c:out value='${theme}' />">

<head>
    <title>
        <spring:message code="settings.header.title"/>
    </title>
    <uv:custom-head/>
    <uv:asset-dependencies-preload asset="settings-form.js" />
    <script type="module" src="<asset:url value='settings-form.js' />"></script>
</head>

<body>

<uv:menu/>

<spring:url var="URL_PREFIX" value="/web"/>

<h1 class="tw-sr-only"><spring:message code="settings.header.title" /></h1>

<div class="content">
    <div class="container">
        <form:form method="POST" action="${URL_PREFIX}/settings" modelAttribute="settings" class="form-horizontal"
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

            <c:set var="applicationError">
                <form:errors path="applicationSettings.*"/>
            </c:set>
            <c:set var="sickNoteError">
                <form:errors path="sickNoteSettings.*"/>
            </c:set>
            <c:set var="accountError">
                <form:errors path="accountSettings.*"/>
            </c:set>

            <c:set var="workingTimeError">
                <form:errors path="workingTimeSettings.*"/>
            </c:set>
            <c:set var="timeError">
                <form:errors path="timeSettings.*"/>
            </c:set>
            <c:set var="overtimeError">
                <form:errors path="overtimeSettings.*"/>
            </c:set>

            <c:set var="calendarError">
                <form:errors path="calendarSettings.*"/>
            </c:set>

            <c:set var="hasAbsenceError" value="${not empty applicationError || not empty sickNoteError || not empty accountError}" />
            <c:set var="hasPublicHolidayError" value="${not empty workingTimeError || not empty timeError || not empty overtimeError}" />
            <c:set var="hasCalendarError" value="${not empty calendarError}" />

            <div class="row">
                <div class="col-xs-12">
                    <ul class="nav nav-tabs" role="tablist">
                        <li role="presentation" class="active">
                            <a
                                href="#absence"
                                aria-controls="absence"
                                role="tab"
                                data-toggle="tab"
                                class="${hasAbsenceError ? 'tw-text-red-800' : ''}"
                            >
                                <spring:message code="settings.tabs.absence"/>
                                <c:if test="${hasAbsenceError}">*</c:if>
                            </a>
                        </li>
                        <li role="presentation">
                            <a
                                href="#absenceTypes"
                                aria-controls="absenceTypes"
                                role="tab"
                                data-toggle="tab"
                            >
                                <spring:message code="settings.tabs.absenceTypes"/>
                            </a>
                        </li>
                        <li role="presentation">
                            <a
                                href="#publicHolidays"
                                aria-controls="publicHolidays"
                                role="tab"
                                data-toggle="tab"
                                class="${hasPublicHolidayError ? 'tw-text-red-800' : ''}"
                                data-test-id="settings-tab-working-time"
                            >
                                <spring:message code="settings.tabs.workingTime"/>
                                <c:if test="${hasPublicHolidayError}">*</c:if>
                            </a>
                        </li>
                        <li role="presentation">
                            <a
                                href="#calendar"
                                aria-controls="calendar"
                                role="tab"
                                data-toggle="tab"
                                class="${hasCalendarError ? 'tw-text-red-800' : ''}"
                            >
                                <spring:message code="settings.tabs.calendar"/>
                                <c:if test="${hasCalendarError}">*</c:if>
                            </a>
                        </li>
                    </ul>
                </div>
            </div>

            <div class="tab-content tw-mb-16">

                <div class="tab-pane active" id="absence">
                    <div class="form-section tw-mb-8">
                        <jsp:include page="absence/vacation.jsp" />
                    </div>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="absence/reminder-waiting-application.jsp" />
                    </div>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="absence/reminder-holiday-replacement.jsp" />
                    </div>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="absence/reminder-upcoming-application.jsp" />
                    </div>
                    <div class="form-section">
                        <jsp:include page="absence/sick-days.jsp" />
                    </div>
                </div>

                <div class="tab-pane" id="absenceTypes">
                    <div class="form-section tw-mb-12 tw-relative tw-z-10">
                        <jsp:include page="absence-types/absence-types.jsp" />
                    </div>
                    <div class="form-section">
                        <jsp:include page="absence-types/special-leave.jsp" />
                    </div>
                </div>

                <div class="tab-pane" id="publicHolidays">
                    <c:if test="${defaultWorkingTimeFromSettings}">
                    <div class="form-section tw-mb-8">
                        <jsp:include page="public-holidays/default-workingtime.jsp" />
                    </div>
                    </c:if>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="public-holidays/time.jsp" />
                    </div>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="public-holidays/public-holidays.jsp" />
                    </div>
                    <div class="form-section tw-mb-8">
                        <jsp:include page="public-holidays/overtime.jsp" />
                    </div>
                </div>

                <div class="tab-pane" id="calendar">
                    <div class="alert alert-danger tw-flex tw-items-center" role="alert">
                        <icon:speakerphone className="tw-w-4 tw-h-4" solid="true" />
                        &nbsp;<spring:message code="settings.calendar.deprecated"/>
                    </div>
                    <div class="form-section">
                        <jsp:include page="calendar/integration.jsp" />
                    </div>
                    <div class="form-section" id="exchange-calendar">
                        <jsp:include page="calendar/exchange.jsp" />
                    </div>
                    <div class="form-section" id="google-calendar">
                        <jsp:include page="calendar/google.jsp" />
                    </div>
                </div>
            </div>

            <div class="form-section">
                <div class="row tw-mb-16">
                    <div class="col-xs-12">
                        <p class="help-block tw-text-sm"><spring:message code="settings.action.update.description"/></p>
                        <button type="submit" class="button-main-green pull-left col-xs-12 col-sm-5 col-md-2" data-test-id="settings-save-button">
                            <spring:message code='action.save'/>
                        </button>
                    </div>
                </div>
            </div>
        </form:form>
    </div>
</div>

<footer>
    <div class="tw-mb-4">
        <span class="tw-text-sm">
            <a href="https://urlaubsverwaltung.cloud/">urlaubsverwaltung.cloud</a> |
        </span>
        <span class="tw-text-xs">
            v${version}
        </span>
    </div>
</footer>

</body>
</html>
