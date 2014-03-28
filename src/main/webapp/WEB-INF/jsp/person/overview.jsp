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

        <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>
        <script>
            $(function() {
                var datepickerLocale = "${pageContext.request.locale.language}";
                var personId = '<c:out value="${person.id}" />';
                var urlPrefix = "<spring:url value='/web' />";

                var year = getUrlParam("year");
                var date;
                var defaultDate;
                
                if(year.length > 0) {
                    date = Date.today().set({ year: parseInt(year), month: 0, day: 1 });
                    defaultDate = Date.today().set({ year: parseInt(year), month: 0, day: 1 });
                } else {
                    date = Date.today();
                    defaultDate = Date.today();
                }

                fetchHighlightedDays(date, urlPrefix + "/calendar/", personId);
                createDatepickerForVacationOverview("#datepicker", datepickerLocale, urlPrefix, personId, defaultDate);

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


