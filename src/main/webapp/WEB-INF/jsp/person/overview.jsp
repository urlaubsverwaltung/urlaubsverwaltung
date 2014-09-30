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
<spring:url var="formUrlPrefix" value="/web"/>

<uv:menu />

<div class="content">

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

            <div class="col-xs-12 col-sm-4">
                <div class="box">
                    <span class="thirds">
                        <img class="box-image img-circle print--invisible" src="<c:out value='${gravatar}?d=mm&s=80'/>"/>
                        <i class="fa fa-at"></i> <c:out value="${person.loginName}"/>
                        <h4><c:out value="${person.niceName}"/></h4>
                        <i class="fa fa-envelope"></i> <c:out value="${person.email}"/>
                    </span>
                </div>
            </div>

            <div class="col-xs-12 col-sm-4">
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

            <div class="col-xs-12 col-sm-4">
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
                                        <spring:message code="overview.vacation.left.remaining.expired" />
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

                var datepickerLocale = "${pageContext.request.locale.language}";
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

                    $.when(
                        holidayService.fetchPublic   ( date.year() ),
                        holidayService.fetchPersonal ( date.year() )
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
                                <a class="btn btn-default pull-right" href="${formUrlPrefix}/application/new">
                                    <i class="fa fa-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <sec:authorize access="hasRole('OFFICE')">
                                    <c:if test="${person.id != loggedUser.id}">
                                        <a class="btn btn-default"
                                           href="${formUrlPrefix}/${person.id}/application/new">
                                            <c:set var="staff" value="${person.firstName} ${person.lastName}"/>
                                            <i class="fa fa-pencil"></i>&nbsp;<spring:message code="ov.apply"/>
                                        </a>
                                    </c:if>
                                </sec:authorize>
                            </c:otherwise>
                        </c:choose>
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

            <c:set var="holidayLeave" value="0" />
            <c:set var="holidayLeaveAllowed" value="0" />
            <c:set var="otherLeave" value="0" />
            <c:set var="otherLeaveAllowed" value="0" />

            <c:forEach items="${applications}" var="app">

                <c:choose>
                    <c:when test="${app.vacationType == 'HOLIDAY'}">
                        <c:if test="${app.status == 'WAITING'}">
                           <c:set var="holidayLeave" value="${holidayLeave + app.days}" />
                        </c:if>
                        <c:if test="${app.status == 'ALLOWED'}">
                            <c:set var="holidayLeave" value="${holidayLeave + app.days}" />
                            <c:set var="holidayLeaveAllowed" value="${holidayLeaveAllowed + app.days}" />
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${app.status == 'WAITING'}">
                            <c:set var="otherLeave" value="${otherLeave + app.days}" />
                        </c:if>
                        <c:if test="${app.status == 'ALLOWED'}">
                            <c:set var="otherLeave" value="${otherLeave + app.days}" />
                            <c:set var="otherLeaveAllowed" value="${otherLeaveAllowed + app.days}" />
                        </c:if>
                    </c:otherwise>
                </c:choose>

            </c:forEach>

            <div class="col-xs-12 col-sm-6">
                <div class="box">
                    <span class="box-icon bg-yellow"><i class="fa fa-sun-o"></i></span>
                    <spring:message code="overview.vacations.holidayLeave" arguments="${holidayLeave}" />
                    <i class="fa fa-check check"></i> <spring:message code="overview.vacations.holidayLeaveAllowed" arguments="${holidayLeaveAllowed}" />
                </div>
            </div>

            <div class="col-xs-12 col-sm-6">
                <div class="box">
                    <span class="box-icon bg-orange"><i class="fa fa-flag-o"></i></span>
                    <spring:message code="overview.vacations.otherLeave" arguments="${otherLeave}" />
                    <i class="fa fa-check state ALLOWED"></i> <spring:message code="overview.vacations.otherLeaveAllowed" arguments="${otherLeaveAllowed}" />
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

                    <legend id="sickNotes">
                        <p>
                            <spring:message code="sicknotes" />
                        </p>
                    </legend>

                </div>
                
            </div>
            
        </div>



        <c:set var="sickNotesDays" value="0" />
        <c:set var="numberOfSickNotesAUB" value="0" />
        <c:set var="sickNotesDaysChild" value="0" />
        <c:set var="numberOfSickNotesChildAUB" value="0" />

        <c:forEach items="${sickNotes}" var="sickNote">

            <c:set var="sickNotesDays" value="${sickNotesDays + sickNote.workDays}" />

            <c:if test="${sickNote.aubPresent}">
                <c:set var="numberOfSickNotesAUB" value="${numberOfSickNotesAUB + 1}" />
            </c:if>

        </c:forEach>
        
        <div class="row">
            <div class="col-xs-12 col-sm-6">
                <div class="box">
                    <span class="thirds">
                       <span class="box-icon bg-red"><i class="fa fa-medkit"></i></span>
                        <spring:message code="overview.sicknotes.sickdays" arguments="${sickNotesDays}" />
                        <i class="fa fa-check state ALLOWED"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${numberOfSickNotesAUB}" />
                    </span>
                </div>
            </div>
            <div class="col-xs-12 col-sm-6">
                <div class="box">
                    <span class="thirds">
                       <span class="box-icon bg-red"><i class="fa fa-child"></i></span>
                        <spring:message code="overview.sicknotes.sickdays.child" arguments="${sickNotesDaysChild}" />
                        <i class="fa fa-check state ALLOWED"></i> <spring:message code="overview.sicknotes.sickdays.aub" arguments="${numberOfSickNotesChildAUB}" />
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


