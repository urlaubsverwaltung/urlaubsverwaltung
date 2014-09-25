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

    <div class="container-fluid">

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

             <div class="col-xs-12 col-sm-4 print-box">

                 <table class="list-table">
                     <tbody>
                     <tr>
                         <td><img class="print--invisible" src="<c:out value='${gravatar}?d=mm&s=80'/>"/></td>
                         <td>
                             <p>@muster</p>
                             <h4><c:out value="${person.niceName}"/></h4>
                             <p><i class="fa fa-envelope"></i> <c:out value="${person.email}"/></p>
                         </td>
                     </tr>
                     </tbody>
                 </table>

             </div>

            <div class="col-xs-12 col-sm-4">
                <table class="list-table">
                <tbody>
                <tr>
                <td>
                    <h1 class="pull-left"><i class="fa fa-calendar"></i>&nbsp;</h1>
                    Es wurden verplant <h4>19 Urlaubstage</h4> (davon sind 20 Tage genehmigt <i class="fa fa-check"></i>)
                </td>
                </tr>
                </tbody>
                </table>
            </div>

            <div class="col-xs-12 col-sm-4">
                <table class="list-table">
                    <tbody>
                    <tr>
                        <td>
                            <h1 class="pull-left"><i class="fa fa-medkit"></i>&nbsp;</h1>
                            Es bestehen <h4>5 Krankheitstage</h4> (davon sind 2 Tage Kind-Krankheitstage <i class="fa fa-child"></i>)
                        </td>
                    </tr>
                    </tbody>
                </table>
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

                    <legend id="sickNotes">
                        <p>
                            <spring:message code="apps.vac" />
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

            <div class="col-xs-12 col-sm-6">
                <%@include file="./include/overview_app_list.jsp" %>
            </div>

            <div class="col-xs-12 col-sm-3">
                <table class="list-table">
                    <tbody>
                    <tr>
                        <td>
                            <h1 class="pull-left"><i class="fa fa-calendar-o"></i>&nbsp;</h1>
                            <p>Es besteht ein <b>Anspruch</b> von <h4>19 Urlaubstagen</h4> (davon sind 4 Tage Resturlaub)</p>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="col-xs-12 col-sm-3">
                <table class="list-table">
                    <tbody>
                    <tr>
                        <td>
                            <h1 class="pull-left"><i class="fa fa-tasks"></i>&nbsp;</h1>
                            <p>Es <b>verbleiben</b> <h4>15 Urlaubstage</h4> (davon sind 0 Tage Resturlaub)</p>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="col-xs-12 col-sm-6">
                <div id="datepicker"></div>
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


