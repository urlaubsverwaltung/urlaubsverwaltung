<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="icon" tagdir="/WEB-INF/tags/icons" %>
<%@taglib prefix="asset" uri="/WEB-INF/asset.tld" %>

<!DOCTYPE html>
<html lang="${language}">

<head>
    <title>
        <c:choose>
            <c:when test="${application.id == null}">
                <spring:message code="application.data.header.title.new"/>
            </c:when>
            <c:otherwise>
                <spring:message code="application.data.header.title.edit"/>
            </c:otherwise>
        </c:choose>
    </title>
    <uv:custom-head/>
    <script>
        if (!window.uv) {
            window.uv = {};
        }
        window.uv.personId = '<c:out value="${person.id}" />';
        window.uv.webPrefix = "<spring:url value='/web' />";
        window.uv.apiPrefix = "<spring:url value='/api' />";
        // 0=sunday, 1=monday
        window.uv.weekStartsOn = 1;

        window.uv.i18n = [];
        window.uv.i18n['application.status.allowed'] = "<spring:message code='ALLOWED' javaScriptEscape='true' />";
        window.uv.i18n['application.applier.applicationsOfColleagues'] = "<spring:message code='application.applier.applicationsOfColleagues' javaScriptEscape='true' />";
        window.uv.i18n['application.applier.none'] = "<spring:message code='application.applier.none' javaScriptEscape='true' />";
        window.uv.i18n['application.applier.invalidPeriod'] = "<spring:message code='application.applier.invalidPeriod' javaScriptEscape='true' />";
        window.uv.i18n['application.applier.day'] = "<spring:message code='application.applier.day' javaScriptEscape='true' />";
        window.uv.i18n['application.applier.days'] = "<spring:message code='application.applier.days' javaScriptEscape='true' />";
    </script>
    <uv:datepicker-localisation/>
    <link rel="stylesheet" type="text/css"
          href="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.css' />"/>
    <link rel="stylesheet" type="text/css"
          href="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.css' />"/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='npm.timepicker.css' />"/>
    <link rel="stylesheet" type="text/css" href="<asset:url value='npm.duetds.css' />"/>
    <script defer src="<asset:url value='npm.duetds.js' />"></script>
    <script defer src="<asset:url value='npm.date-fns.js' />"></script>
    <script defer src="<asset:url value='npm.timepicker.js' />"></script>
    <script defer src="<asset:url value='app_detail~app_form~person_overview.js' />"></script>
    <script defer
            src="<asset:url value='account_form~app_form~app_statistics~overtime_form~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer
            src="<asset:url value='account_form~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_notes~workingtime_form.js' />"></script>
    <script defer
            src="<asset:url value='account_form~app_detail~app_form~app_statistics~overtime_form~person_overview~sick_note_form~sick_no~95889e93.js' />"></script>
    <script defer src="<asset:url value='app_form.js' />"></script>
</head>

<body>

<spring:url var="URL_PREFIX" value="/web"/>
<c:set var="DATE_PATTERN">
    <spring:message code="pattern.date"/>
</c:set>
<c:set var="TIME_PATTERN">
    <spring:message code="pattern.time"/> <spring:message code="application.data.time.placeholder"/>
</c:set>
<uv:menu/>

<c:choose>
    <c:when test="${application.id == null}">
        <c:set var="ACTION" value="${URL_PREFIX}/application"/>
        <c:set var="ADD_REPLACEMENT_ACTION" value="${URL_PREFIX}/application/new"/>
        <c:set var="heading">
            <spring:message code="application.data.title"/>
        </c:set>
    </c:when>
    <c:otherwise>
        <c:set var="ACTION" value="${URL_PREFIX}/application/${application.id}"/>
        <c:set var="ADD_REPLACEMENT_ACTION" value="${URL_PREFIX}/application/${application.id}"/>
        <c:set var="heading">
            <spring:message code="application.data.title.edit"/>
        </c:set>
    </c:otherwise>
</c:choose>

<c:set var="IS_SPECIALLEAVE_SHOWN" value="${application.vacationType.category == 'SPECIALLEAVE' ? '' : 'hidden'}"/>
<c:set var="IS_OVERTIME_SHOWN" value="${application.vacationType.category == 'OVERTIME' ? '' : 'hidden'}"/>
<c:set var="IS_DAYS_COUNT_SHOWN" value="hidden"/>

<div class="content">
    <div class="container">
        <c:choose>
            <c:when test="${noHolidaysAccount}">
                <spring:message code="application.applier.account.none"/>
            </c:when>
            <c:otherwise>
                <form:form id="applicationForm" method="POST" action="${ACTION}" modelAttribute="application"
                           class="form-horizontal" role="form">
                    <form:hidden path="id" value="${application.id}"/>
                    <form:hidden path="person" value="${person.id}"/>

                    <c:if test="${not empty errors.globalErrors}">
                        <div class="row">
                            <div class="col-xs-12 alert alert-danger">
                                <form:errors/>
                            </div>
                        </div>
                    </c:if>

                    <div class="form-section tw-mb-8">
                        <uv:section-heading>
                            <h1>
                                    ${heading}
                            </h1>
                        </uv:section-heading>

                        <div class="row">
                            <c:if test="${isOffice}">
                                <div class="col-md-8">
                                <c:choose>
                                    <c:when test="${application.id == null}">
                                            <%-- office applies for a user --%>
                                            <div class="form-group is-required">
                                                <label class="control-label col-md-3">
                                                    <spring:message code="application.data.person"/>
                                                </label>
                                                <div class="col-md-9">
                                                    <uv:select id="person-select" name=""
                                                               onchange="window.location.href=this.options[this.selectedIndex].value">
                                                        <c:forEach items="${persons}" var="p">
                                                            <option
                                                                value="${URL_PREFIX}/application/new?person=${p.id}" ${person.id == p.id ? 'selected="selected"' : ''}>
                                                                <c:out value="${p.niceName}"/>
                                                            </option>
                                                        </c:forEach>
                                                    </uv:select>
                                                </div>
                                            </div>
                                    </c:when>
                                    <c:otherwise>
                                        <%-- office can not edit user --%>
                                        <div class="form-group">
                                            <label class="control-label col-md-3">
                                                <spring:message code="application.data.person"/>
                                            </label>
                                            <div class="col-md-9">
                                                <p class="form-control-static"><c:out value="${application.person.niceName}"/></p>
                                            </div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                                </div>
                            </c:if>
                        </div>

                            <%-- Vacation Type--%>
                        <div class="row">
                            <div class="col-md-8">
                                <div class="form-group is-required">
                                    <label class="control-label col-md-3" for="vacationType">
                                        <spring:message code="application.data.vacationType"/>:
                                    </label>
                                    <div class="col-md-9">
                                        <uv:select id="vacationType" name="vacationType"
                                                   onchange="vacationTypeChanged(this.value);">
                                            <c:forEach items="${vacationTypes}" var="vacationType">
                                                <c:choose>
                                                    <c:when test="${vacationType == application.vacationType}">
                                                        <option value="${vacationType.id}" selected="selected">
                                                            <spring:message code="${vacationType.messageKey}"/>
                                                        </option>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <option value="${vacationType.id}">
                                                            <spring:message code="${vacationType.messageKey}"/>
                                                        </option>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </uv:select>
                                    </div>
                                </div>
                            </div>
                        </div>

                            <%-- start and end date--%>
                        <div class="row">

                            <div class="col-md-8">
                                <div class="form-group is-required">
                                    <label class="control-label col-xs-12 col-md-3" for="from">
                                        <spring:message code="absence.period.startDate"/>:
                                    </label>
                                    <div class="col-xs-8 col-md-5">
                                        <form:input id="from" path="startDate" class="form-control"
                                                    cssErrorClass="form-control error" placeholder="${DATE_PATTERN}"
                                                    autocomplete="off"
                                                    data-iso-value="${application.startDateIsoValue}"/>
                                    </div>
                                    <div class="col-xs-4 col-md-4">
                                        <form:input id="startTime" path="startTime" class="form-control"
                                                    cssErrorClass="form-control error" placeholder="${TIME_PATTERN}"
                                                    autocomplete="off"/>
                                    </div>
                                </div>

                                <div class="form-group is-required">
                                    <label class="control-label col-xs-12 col-md-3" for="to">
                                        <spring:message code="absence.period.endDate"/>:
                                    </label>
                                    <div class="col-xs-8 col-md-5">
                                        <form:input id="to" path="endDate" class="form-control"
                                                    cssErrorClass="form-control error" placeholder="${DATE_PATTERN}"
                                                    autocomplete="off" data-iso-value="${application.endDateIsoValue}"/>
                                    </div>
                                    <div class="col-xs-4 col-md-4">
                                        <form:input id="endTime" path="endTime" class="form-control"
                                                    cssErrorClass="form-control error" placeholder="${TIME_PATTERN}"
                                                    autocomplete="off"/>
                                    </div>
                                </div>

                                <div id="days-count" class="form-group ${IS_DAYS_COUNT_SHOWN}">
                                    <div class="col-md-9 col-md-offset-3">
                                        <span class="info days tw-text-sm"></span>
                                    </div>
                                </div>
                            </div>

                            <div class="col-md-4">
                                <div class="form-group">
                                    <div class="col-xs-9">
                                        <span id="departmentVacations" class="help-block info tw-text-sm"></span>
                                    </div>
                                </div>
                            </div>
                        </div>

                            <%-- Absence Period --%>
                        <c:choose>
                        <c:when test="${showHalfDayOption}">
                            <div class="row">
                                <div class="col-md-8">
                                    <div class="form-group is-required">
                                        <label class="control-label col-md-3">
                                            <spring:message code="absence.period"/>:
                                        </label>
                                        <div class="col-md-9 radio">
                                            <label class="thirds">
                                                <form:radiobutton id="fullDay" class="dayLength-full" path="dayLength"
                                                                  checked="checked" value="FULL"/>
                                                <spring:message code="FULL"/>
                                            </label>
                                            <label class="thirds">
                                                <form:radiobutton id="morning" class="dayLength-half" path="dayLength"
                                                                  value="MORNING"/>
                                                <spring:message code="MORNING"/>
                                            </label>
                                            <label class="thirds">
                                                <form:radiobutton id="noon" class="dayLength-half" path="dayLength"
                                                                  value="NOON"/>
                                                <spring:message code="NOON"/>
                                            </label>
                                        <uv:error-text>
                                              <form:errors path="dayLength" />
                                            </uv:error-text>
                                        </div>
                                </div>
                            </div>
                        </div></c:when>
                        <c:otherwise>
                            <form:hidden path="dayLength" value="FULL" />
                        </c:otherwise>
                    </c:choose>

                        <%-- Overtime Information--%>
                        <c:if test="${overtimeActive}">
                            <div class="row ${IS_OVERTIME_SHOWN}" id="overtime">
                                <div class="col-md-8">
                                    <div class="form-group is-required" id="form-group--hours">
                                        <label class="control-label col-md-3" for="hours">
                                            <spring:message code="application.data.hours"/>:
                                        </label>
                                        <div class="col-md-9">
                                            <uv:hour-and-minute-input/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>

                            <%-- Special Leave Information --%>
                        <div class="row ${IS_SPECIALLEAVE_SHOWN}" id="special-leave">
                            <div class="col-md-8">
                                <div class="form-group is-required" id="form-group--reason">
                                    <label class="control-label col-md-3" for="reason">
                                        <spring:message code="application.data.reason"/>:
                                    </label>
                                    <div class="col-md-9">
                                        <small>
                                            <span id="text-reason"></span><spring:message
                                            code="action.comment.maxChars"/>
                                        </small>
                                        <form:textarea id="reason" rows="1" path="reason" class="form-control"
                                                       cssErrorClass="form-control error"
                                                       onkeyup="count(this.value, 'text-reason');"
                                                       onkeydown="maxChars(this,200); count(this.value, 'text-reason');"/>
                                        <uv:error-text>
                                            <form:errors path="reason"/>
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6 col-md-push-2">
                            <span class="help-block tw-text-sm">
                                <icon:information-circle className="tw-w-4 tw-h-4" solid="true"/>
                                <spring:message code="application.data.description"/>
                            </span>
                            </div>
                        </div>
                    </div>

                    <div class="form-section tw-mb-16">
                        <uv:section-heading>
                            <h2>
                                <spring:message code="application.data.furtherInformation.title"/>
                            </h2>
                        </uv:section-heading>

                        <div class="row">
                            <div class="col-md-8">

                                    <%-- agreed wiht team --%>
                                <div class="form-group">
                                    <label class="control-label col-md-3">
                                        <spring:message code="application.data.teamInformed"/>:
                                    </label>
                                    <div class="col-md-9 radio">
                                        <label class="thirds">
                                            <form:radiobutton id="teamInformed" path="teamInformed" value="true"/>
                                            <spring:message code="application.data.teamInformed.true"/>
                                        </label>
                                        <label class="thirds">
                                            <form:radiobutton id="teamNotInformed" path="teamInformed" value="false"/>
                                            <spring:message code="application.data.teamInformed.false"/>
                                        </label>
                                        <label class="thirds"></label>
                                        <uv:error-text>
                                            <form:errors path="teamInformed"/>
                                        </uv:error-text>
                                    </div>
                                </div>

                                <%-- holiday replacement--%>
                                <c:if test="${not empty selectableHolidayReplacements}">
                                    <div class="form-group">
                                        <label class="control-label col-md-3">
                                            <spring:message code="application.data.holidayReplacement"/>
                                        </label>
                                        <div class="col-md-9">
                                            <div class="tw-flex">
                                                <uv:select
                                                    id="holiday-replacement-select"
                                                    name="holidayReplacementToAdd"
                                                    cssClass="tw-rounded-l tw-rounded-r-none"
                                                >
                                                    <option value=""></option>
                                                    <c:forEach items="${selectableHolidayReplacements}" var="person">
                                                        <option value="${person.personId}">
                                                            <c:out value="${person.displayName}"/>
                                                        </option>
                                                    </c:forEach>
                                                </uv:select>
                                                <button
                                                    type="submit"
                                                    class="btn btn-default tw-border-l-0 tw-rounded-r tw-m-0 tw-px-4 tw-font-medium"
                                                    name="add-holiday-replacement"
                                                    formmethod="post"
                                                    formaction="${ADD_REPLACEMENT_ACTION}"
                                                >
                                                    <spring:message code="application.data.holidayReplacement.add-button.text" />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>

                                <c:if test="${not empty application.holidayReplacements}">
                                    <div class="tw-mb-12">
                                    <c:forEach items="${application.holidayReplacements}" var="holidayReplacement" varStatus="loop">
                                        <form:hidden path="holidayReplacements[${loop.index}].person" />
                                        <div id="holidayReplacementNoteRow" class="form-group tw-mb-6">
                                            <div class="col-md-push-3 col-md-9">
                                                <div>
                                                    <div class="tw-flex">
                                                        <img
                                                            src="<c:out value='${holidayReplacement.person.gravatarURL}?d=mm&s=40'/>"
                                                            alt="<spring:message code="gravatar.alt" arguments="${holidayReplacement.person.niceName}"/>"
                                                            class="gravatar tw-rounded-full tw-mr-4 tw-mt-1"
                                                            width="40px"
                                                            height="40px"
                                                            onerror="this.src !== '/images/gravatar.jpg' && (this.src = '/images/gravatar.jpg')"
                                                        />
                                                        <div>
                                                            <div class="tw-flex tw-items-center tw-flex-wrap">
                                                                <span>
                                                                    <c:out value="${holidayReplacement.person.niceName}" />
                                                                </span>
                                                                <c:if test="${not empty holidayReplacement.departments}">
                                                                    <ul class="tw-m-0 tw-ml-1 tw-p-0 tw-list-none tw-flex tw-flex-wrap tw-text-xs tw-space-x-0.5">
                                                                        <c:forEach items="${holidayReplacement.departments}" var="department">
                                                                            <li class="tw-px-1.5 tw-rounded-full tw-bg-green-100 tw-text-green-800">
                                                                                <c:out value="${department}" />
                                                                            </li>
                                                                        </c:forEach>
                                                                    </ul>
                                                                </c:if>
                                                            </div>
                                                            <div class="tw-flex">
                                                                <button
                                                                    type="submit"
                                                                    class="tw-p-0 tw-bg-transparent"
                                                                    name="remove-holiday-replacement"
                                                                    value="${holidayReplacement.person.id}"
                                                                    formmethod="post"
                                                                    formaction="${ADD_REPLACEMENT_ACTION}"
                                                                >
                                                                    <span class="tw-flex tw-items-center tw-text-sm tw-text-black tw-text-opacity-50 hover:tw-text-opacity-100 focus:tw-text-opacity-100 tw-transition-colors">
                                                                        <icon:trash className="tw-w-4 tw-h-4 tw-mr-0.5" />
                                                                        <spring:message code="application.data.holidayReplacement.remove-button.text" />
                                                                    </span>
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div class="tw-mt-2 md:tw-pl-14">
                                                        <div class="tw-flex tw-justify-between tw-items-center">
                                                            <label for="replacement-note-${loop.index}" class="tw-text-sm tw-text-black tw-text-opacity-50 tw-mb-0 tw-font-normal">
                                                                <spring:message code="application.data.holidayReplacementNote" arguments="${holidayReplacement.person.firstName}" />
                                                            </label>
                                                            <span>
                                                                <small class="tw-flex tw-justify-between tw-text-sm tw-text-black tw-text-opacity-50">
                                                                    <span class="tw-flex-grow"></span>
                                                                    <span id="text-holiday-replacement-note-${loop.index}"></span>
                                                                    <spring:message code="action.comment.maxChars"/>
                                                                </small>
                                                            </span>
                                                        </div>
                                                        <div>
                                                            <form:textarea
                                                                id="replacement-note-${loop.index}"
                                                                rows="1"
                                                                path="holidayReplacements[${loop.index}].note"
                                                                class="form-control"
                                                                cssErrorClass="form-control error"
                                                                onkeyup="count(this.value, 'text-holiday-replacement-note-${loop.index}');"
                                                                onkeydown="maxChars(this,200); count(this.value, 'text-holiday-replacement-note-${loop.index}');"
                                                            />
                                                        </div>
                                                    </div>
                                                </div>
                                                <uv:error-text>
                                                    <form:errors path="holidayReplacements[${loop.index}].note"/>
                                                </uv:error-text>
                                            </div>
                                        </div>
                                    </c:forEach>
                                    </div>
                                </c:if>

                                <%-- Address and phone number--%>
                                <div class="form-group">
                                    <label class="control-label col-md-3" for="address">
                                        <spring:message code="application.data.furtherInformation.address"/>:
                                    </label>
                                    <div class="col-md-9">
                                        <form:textarea id="address" rows="1" path="address" class="form-control"
                                                       cssErrorClass="form-control error"
                                                       onkeyup="count(this.value, 'text-address');"
                                                       onkeydown="maxChars(this,200); count(this.value, 'text-address');"/>
                                        <div class="tw-mt-1">
                                            <small class="tw-flex tw-justify-between tw-text-sm tw-text-black tw-text-opacity-50">
                                                <span class="tw-flex-grow"></span>
                                                <span id="text-address"></span><spring:message
                                                code="action.comment.maxChars"/>
                                            </small>
                                        </div>
                                        <uv:error-text>
                                            <form:errors path="address"/>
                                        </uv:error-text>
                                    </div>
                                </div>

                                <%-- Comment --%>
                                <div class="form-group">
                                    <label class="control-label col-md-3" for="comment">
                                        <spring:message code="application.data.furtherInformation.comment"/>:
                                    </label>
                                    <div class="col-md-9">
                                        <form:textarea id="comment" rows="1" path="comment" class="form-control"
                                                       cssErrorClass="form-control error"
                                                       onkeyup="count(this.value, 'text-comment');"
                                                       onkeydown="maxChars(this,200); count(this.value, 'text-comment');"/>
                                        <div class="tw-mt-1">
                                            <small class="tw-flex tw-justify-between tw-text-sm tw-text-black tw-text-opacity-50">
                                                <span class="tw-flex-grow"></span>
                                                <span id="text-comment"></span><spring:message
                                                code="action.comment.maxChars"/>
                                            </small>
                                        </div>
                                        <uv:error-text>
                                            <form:errors path="comment"/>
                                        </uv:error-text>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="form-section">
                        <div class="row">
                            <div class="col-xs-12">
                                <hr/>
                                <button id="apply-application" type="submit"
                                        class="btn btn-success pull-left col-xs-12 col-sm-5 col-md-2">
                                    <spring:message code="action.apply.vacation"/>
                                </button>
                                <button type="button"
                                        class="btn btn-default back col-xs-12 col-sm-5 col-md-2 pull-right">
                                    <spring:message code="action.cancel"/>
                                </button>
                            </div>
                        </div>
                    </div>
                </form:form>
            </c:otherwise>
        </c:choose>
    </div>
    <!-- End of grid container -->

</div>
</body>
</html>
