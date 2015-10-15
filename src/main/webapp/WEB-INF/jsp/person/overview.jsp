<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>

<head>
    <uv:head />
    <script type="text/javascript">
        /**
         * @param {string|{}} data
         * @param {string} [data.src]
         * @param {string} [data.fallback]
         * @return {$.Deferred}
         */
        function addScript(data) {

            var deferred = $.Deferred();
            var isFallback = typeof data === 'string';

            var script  = document.createElement('script');
            script.src  = isFallback ? data : data.src;
            script.type = 'text/javascript';

            script.onload  = function() {
                deferred.resolve();
            };

            script.onerror = function() {
                if (isFallback) {
                    deferred.reject();
                } else {
                    addScript(data.fallback).then(deferred.resolve);
                }
            };

            document.getElementsByTagName('head')[0].appendChild(script);

            return deferred.promise();
        }
    </script>
</head>

<body>
<spring:url var="URL_PREFIX" value="/web"/>

<uv:menu />

<div class="print-info--only-portrait">
    <h4><spring:message code="print.info.portrait" /></h4>
</div>

<div class="content print--only-portrait">

    <div class="container">

        <div class="row">

            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${!empty param.year}">
                        <c:set var="displayYear" value="${param.year}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="displayYear" value="${year}"/>
                    </c:otherwise>
                </c:choose>

                <%@include file="./include/overview_header.jsp" %>
            </div>
            
        </div>
        
        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <div class="box-image gravatar img-circle hidden-print" data-gravatar="<c:out value='${person.gravatarURL}?d=mm&s=60'/>"></div>
                    <span class="box-text">
                        <i class="fa fa-at"></i> <c:out value="${person.loginName}"/>
                        <h4><c:out value="${person.niceName}"/></h4>
                        <c:if test="${person.email != null}">
                            <i class="fa fa-envelope-o"></i> <a href="mailto:<c:out value='${person.email}'/>"><c:out value="${person.email}"/></a>
                        </c:if>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-calendar"></i></span>
                    <span class="box-text">
                        <c:choose>
                            <c:when test="${account != null}">
                                <spring:message code="person.account.vacation.entitlement" arguments="${account.vacationDays}" />
                                <spring:message code="person.account.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}" />
                            </c:when>
                            <c:otherwise>
                                <spring:message code='person.account.vacation.noInformation'/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-bar-chart"></i></span>
                    <span class="box-text">
                        <c:choose>
                            <c:when test="${account != null}">
                                <spring:message code="person.account.vacation.left" arguments="${vacationDaysLeft.vacationDays}" />
                                <c:choose>
                                    <c:when test="${beforeApril}">
                                        <spring:message code="person.account.vacation.left.remaining" arguments="${vacationDaysLeft.remainingVacationDays}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="person.account.vacation.left.remaining" arguments="${vacationDaysLeft.remainingVacationDaysNotExpiring}" />
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                <spring:message code='person.account.vacation.noInformation'/>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </div>

        </div>

        <div class="row">
            <div class="col-xs-12">
                <legend>
                    <spring:message code="overtime.title"/>
                    <span>
                        <a href="${URL_PREFIX}/overtime" class="fa-action pull-right" style="margin-top: 1px" data-title="<spring:message code="action.overtime.list"/>">
                            <i class="fa fa-th"></i>
                        </a>
                    </span>
                    <span>
                        <a href="${URL_PREFIX}/overtime/new" class="fa-action pull-right" data-title="<spring:message code="action.overtime.new"/>">
                            <i class="fa fa-plus-circle"></i>
                        </a>
                    </span>
                </legend>
            </div>
            <div class="col-xs-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-clock-o"></i></span>
                    <span class="box-text">
                        <spring:message code="overtime.person.total" arguments="${overtimeTotal}"/>
                    </span>
                </div>
            </div>
            <div class="col-xs-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-green">
                        <i class="fa fa-history"></i>
                    </span>
                    <span class="box-text">
                        <spring:message code="overtime.person.left" arguments="${overtimeLeft}"/>
                    </span>
                </div>
            </div>
        </div>

        <script src="<spring:url value='/js/calendar.js' />" type="text/javascript" ></script>
        <script>
            $(function() {

                var datepickerLocale = "${pageContext.response.locale.language}";
                var personId = '<c:out value="${person.id}" />';
                var webPrefix = "<spring:url value='/web' />";
                var apiPrefix = "<spring:url value='/api' />";

                function addMomentScript() {
                    return addScript({
                        src: '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.5.1/moment.min.js',
                        fallback: '<spring:url value='/lib/moment/moment.min.js' />'
                    });
                }

                // dependently of the locale a specific language file is fetched for momentjs
                // fallback is a german language file
                function addMomentLangScript() {
                    return addScript({
                        src: '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.5.1/lang/' + datepickerLocale + '.js',
                        fallback: '<spring:url value='/lib/moment/moment.lang.de.js' />'
                    });
                }

                // calendar is initialised when moment.js AND moment.language.js are loaded
                function initCalendar() {
                    var year = getUrlParam("year");
                    var date = moment();

                    if (year.length > 0) {
                        date.year(year).month(0).date(1);
                    }

                    var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, +personId);

                    // NOTE: All moments are mutable!
                    var startDateToCalculate = date.clone();
                    var endDateToCalculate = date.clone();
                    var shownNumberOfMonths = 10;
                    var startDate = startDateToCalculate.subtract(shownNumberOfMonths/2, 'months');
                    var endDate = endDateToCalculate.add(shownNumberOfMonths/2, 'months');

                    var yearOfStartDate = startDate.year();
                    var yearOfEndDate = endDate.year();

                    $.when(
                        holidayService.fetchPublic   ( yearOfStartDate ),
                        holidayService.fetchPersonal ( yearOfStartDate ),
                        holidayService.fetchSickDays ( yearOfStartDate ),

                        holidayService.fetchPublic   ( yearOfEndDate ),
                        holidayService.fetchPersonal ( yearOfEndDate ),
                        holidayService.fetchSickDays ( yearOfEndDate )
                    ).always(function() {
                        Urlaubsverwaltung.Calendar.init(holidayService, date);
                    });
                }

                addMomentScript().then(addMomentLangScript).then(initCalendar);

                var resizeTimer = null;
                
                $(window).on('resize', function () {

                    if (resizeTimer !== null) {
                        clearTimeout(resizeTimer);
                    }

                    resizeTimer = setTimeout(function () {
                        Urlaubsverwaltung.Calendar.reRender();
                        resizeTimer = null;
                    }, 30)

                });
                
            });
        </script>

        <div class="row">
            <div class="col-xs-12">
                <hr/>
                <div id="datepicker"></div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <legend id="vacation">
                    <spring:message code="applications.title" />
                    <c:choose>
                        <c:when test="${person.id == signedInUser.id}">
                            <a class="fa-action pull-right" href="${URL_PREFIX}/application/new" data-title="<spring:message code="action.apply.vacation"/>">
                                <i class="fa fa-plus-circle"></i>
                            </a>
                        </c:when>
                        <c:otherwise>
                            <sec:authorize access="hasRole('OFFICE')">
                                <a class="fa-action pull-right" href="${URL_PREFIX}/application/new?personId=${person.id}&appliesOnOnesBehalf=true"
                                    data-title="<spring:message code="action.apply.vacation"/>">
                                    <i class="fa fa-plus-circle"></i>
                                </a>
                            </sec:authorize>
                        </c:otherwise>
                    </c:choose>

                </legend>
            </div>
        </div>

        <div class="row">

            <c:set var="holidayLeave" value="${usedDaysOverview.holidayDays.days['WAITING'] + usedDaysOverview.holidayDays.days['ALLOWED'] + 0}" />
            <c:set var="holidayLeaveAllowed" value="${usedDaysOverview.holidayDays.days['ALLOWED'] + 0}" />
            <c:set var="otherLeave" value="${usedDaysOverview.otherDays.days['WAITING'] + usedDaysOverview.otherDays.days['ALLOWED'] + 0}" />
            <c:set var="otherLeaveAllowed" value="${usedDaysOverview.otherDays.days['ALLOWED'] + 0}" />

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow"><i class="fa fa-sun-o"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.holidayLeave" arguments="${holidayLeave}" />
                        <i class="fa fa-check positive"></i> <spring:message code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}" />
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow"><i class="fa fa-flag-o"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}" />
                        <i class="fa fa-check positive"></i> <spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}" />
                    </span>
                </div>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                <%@include file="./include/overview_app_list.jsp" %>
            </div>

        </div>

        <sec:authorize access="hasRole('OFFICE')">
            <c:set var="IS_OFFICE" value="${true}"/>
        </sec:authorize>

        <c:if test="${person.id == signedInUser.id || IS_OFFICE}">

            <div class="row">
                <div class="col-xs-12">
                    <legend id="anchorSickNotes">
                        <spring:message code="sicknotes.title" />
                        <c:if test="${IS_OFFICE}">
                            <a class="fa-action pull-right" href="${URL_PREFIX}/sicknote/new?person=${person.id}"
                               data-title="<spring:message code="action.apply.sicknote" />">
                                <i class="fa fa-plus-circle"></i>
                            </a>
                        </c:if>
                    </legend>
                </div>
            </div>
            
            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red"><i class="fa fa-medkit"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays" arguments="${sickDays}" />
                        <i class="fa fa-check positive"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysWithAUB}" />
                    </span>
                    </div>
                </div>
                <div class="col-xs-12 col-sm-12 col-md-6">
                    <div class="box">
                        <span class="box-icon bg-red"><i class="fa fa-child"></i></span>
                    <span class="box-text">
                        <spring:message code="overview.sicknotes.sickdays.child" arguments="${childSickDays}" />
                        <i class="fa fa-check positive"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${childSickDaysWithAUB}" />
                    </span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <%@include file="./include/sick_notes.jsp" %>
                </div>
            </div>

        </c:if>

    </div>
</div>


</body>


