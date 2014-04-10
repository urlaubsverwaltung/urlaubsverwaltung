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
    <link rel="stylesheet" href="<spring:url value='/css/calendar.css' />">
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
    <style type="text/css">
        .app-detail td {
            width: auto;
        }
        a.ui-state-default {
            border: 1px solid #CCCCCC !important;
            color: #1C94C4 !important;
        }
        
        td.ui-datepicker-today a {
            color: #2C7FB8 !important;
            background: #9ECAE1 !important;
        }
        
    </style>
</head>

<body>
<spring:url var="formUrlPrefix" value="/web"/>

<uv:menu />

<div id="content">

    <div class="container_12">

        <div class="grid_12">

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

        <div class="grid_7 print-box">
            <table class="app-detail" cellspacing="0">
                <%@include file="../application/include/account_days.jsp" %>
            </table>
        </div>
        
        <div class="grid_5 print-box">
            <table class="app-detail" cellspacing="0">
                <%@include file="./include/used_days.jsp" %>
            </table>
        </div>

        <div class="grid_12">&nbsp;</div>
        <div class="grid_12">&nbsp;</div>
        
        <div class="grid_12">
        
            <div id="datepicker"></div>
            
        </div>

        <%--<script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>--%>
        <script src="<spring:url value='/js/calendar.js' />" type="text/javascript" ></script>
        <%--<script src="<spring:url value='/js/moment.lang.de.js' />" type="text/javascript" ></script>--%>
        <script>
            $(function() {

                var datepickerLocale = "${pageContext.request.locale.language}";
                var personId = '<c:out value="${person.id}" />';
                var urlPrefix = "<spring:url value='/web' />";

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

                    var holidayService = Urlaubsverwaltung.HolidayService.create(urlPrefix);

                    $.when(
                        holidayService.fetchPublic   ( date.year() ),
                        holidayService.fetchPersonal ( +personId, date.year() )
                    ).always(function() {
                        Urlaubsverwaltung.Calendar.init(holidayService);
                    });
                }

                addMomentScript().then(addMomentLangScript).then(initCalendar);
            });
        </script>

        <div class="grid_12">&nbsp;</div>
        <div class="grid_12">&nbsp;</div>

        <%@include file="./include/overview_app_list.jsp" %>

        <div class="grid_12 last-element">

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


</body>


