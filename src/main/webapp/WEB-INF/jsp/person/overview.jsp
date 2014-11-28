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

                <c:choose>
                    <c:when test="${person.id == loggedUser.id}">
                        <%@include file="./include/overview_header_user.jsp" %>
                    </c:when>
                    <c:otherwise>
                        <sec:authorize access="hasAnyRole('OFFICE', 'BOSS')">
                            <%@include file="./include/overview_header_office.jsp" %>
                        </sec:authorize>
                    </c:otherwise>
                </c:choose>
            </div>
            
        </div>
        
        <div class="row">

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <span class="thirds">
                        <img class="box-image img-circle hidden-print" src="<c:out value='${gravatar}?d=mm&s=80'/>"/>
                        <i class="fa fa-at"></i> <c:out value="${person.loginName}"/>
                        <h4><c:out value="${person.niceName}"/></h4>
                        <i class="fa fa-envelope"></i> <a href="mailto:<c:out value='${person.email}'/>"><c:out value="${person.email}"/></a>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-calendar"></i></span>
                    <c:choose>
                        <c:when test="${account != null}">
                            <span class="thirds">
                                <spring:message code="overview.vacation.entitlement" arguments="${account.vacationDays}" />
                                <spring:message code="overview.vacation.entitlement.remaining" arguments="${account.remainingVacationDays}" />
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="one"><spring:message code='not.specified'/></span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-4">
                <div class="box">
                    <span class="box-icon bg-green"><i class="fa fa-bar-chart"></i></span>
                    <c:choose>
                        <c:when test="${account != null}">
                            <span class="thirds">
                                <spring:message code="overview.vacation.left" arguments="${leftDays}" />
                                <c:choose>
                                    <c:when test="${beforeApril || !account.remainingVacationDaysExpire}">
                                        <spring:message code="overview.vacation.left.remaining" arguments="${remLeftDays}" />
                                    </c:when>
                                    <c:otherwise>
                                        <spring:message code="overview.vacation.left.remaining.expired" arguments="${remLeftDays}" />
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="one"><spring:message code='not.specified'/></span>
                        </c:otherwise>
                    </c:choose>
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
                        fallback: '<spring:url value='/moment.min.js' />'
                    });
                }

                // dependently of the locale a specific language file is fetched for momentjs
                // fallback is a german language file
                function addMomentLangScript() {
                    return addScript({
                        src: '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.5.1/lang/' + datepickerLocale + '.js',
                        fallback: '<spring:url value='/js/moment.lang.de.js' />'
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

                    var yearToFetchFor = date.year();

                    // TODO: it's not nice at all to fetch holidays for two years...would be better if the methods to fetch holidays get a date instead of a year
                    $.when(
                        holidayService.fetchPublic   ( yearToFetchFor ),
                        holidayService.fetchPublic   ( yearToFetchFor+1 ),
                        holidayService.fetchPersonal ( yearToFetchFor ),
                        holidayService.fetchPersonal ( yearToFetchFor+1 )
                    ).always(function() {
                        Urlaubsverwaltung.Calendar.init(holidayService);
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

                <div class="header">

                    <legend id="vacation">
                        <p>
                            <spring:message code="applications" />
                        </p>
                        <c:choose>
                            <c:when test="${person.id == loggedUser.id}">
                                <c:set var="NEW_APPLICATION_URL" value ="${URL_PREFIX}/application/new" />
                            </c:when>
                            <c:otherwise>
                                <sec:authorize access="hasRole('OFFICE')">
                                    <c:if test="${person.id != loggedUser.id}">
                                        <c:set var="NEW_APPLICATION_URL" value ="${URL_PREFIX}/${person.id}/application/new" />
                                    </c:if>
                                </sec:authorize>
                            </c:otherwise>
                        </c:choose>
                        <a class="btn btn-default pull-right" href="${NEW_APPLICATION_URL}">
                            <i class="fa fa-pencil"></i> <span class="hidden-xs"><spring:message code="ov.apply"/></span>
                        </a>


                    </legend>

                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-xs-12">
                <div id="datepicker"></div>
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
                    <spring:message code="overview.vacations.holidayLeave" arguments="${holidayLeave}" />
                    <i class="fa fa-check check"></i> <spring:message code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}" />
                </div>
            </div>

            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="box-icon bg-yellow"><i class="fa fa-flag-o"></i></span>
                    <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}" />
                    <i class="fa fa-check check"></i> <spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}" />
                </div>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                <%@include file="./include/overview_app_list.jsp" %>
            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">

                <div class="header">

                    <legend id="anchorSickNotes">
                        <p>
                            <spring:message code="sicknotes" />
                        </p>
                        <sec:authorize access="hasRole('OFFICE')">
                            <a class="btn btn-default pull-right" href="${URL_PREFIX}/sicknote/new?person=${person.id}">
                                <i class="fa fa-plus"></i> <span class="hidden-xs"><spring:message code="sicknotes.new" /></span>
                            </a>
                        </sec:authorize>
                    </legend>

                </div>
                
            </div>
            
        </div>

        <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="thirds">
                       <span class="box-icon bg-red"><i class="fa fa-medkit"></i></span>
                        <spring:message code="overview.sicknotes.sickdays" arguments="${sickDays}" />
                        <i class="fa fa-check check"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${sickDaysWithAUB}" />
                    </span>
                </div>
            </div>
            <div class="col-xs-12 col-sm-12 col-md-6">
                <div class="box">
                    <span class="thirds">
                       <span class="box-icon bg-red"><i class="fa fa-child"></i></span>
                        <spring:message code="overview.sicknotes.sickdays.child" arguments="${childSickDays}" />
                        <i class="fa fa-check check"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${childSickDaysWithAUB}" />
                    </span>
                </div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-xs-12">
                <c:choose>
                    <c:when test="${person.id == loggedUser.id}">
                        <%@include file="./include/sick_notes.jsp" %>
                    </c:when>
                    <c:otherwise>
                        <sec:authorize access="hasRole('OFFICE')">
                            <%@include file="./include/sick_notes.jsp" %>
                        </sec:authorize>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>
</div>


</body>


