
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<!DOCTYPE html>
<html>

<head>
    <title><spring:message code="title" /></title>
    <%@include file="../include/header.jsp" %>
</head>

<body>

<spring:url var="formUrlPrefix" value="/web" />

<%@include file="../include/menu_header.jsp" %>

<div id="content">
    <div class="container_12">

        <div class="grid_12">

            <div class="overview-header">

                <legend style="margin-bottom: 0">
                    <p>
                        <spring:message code="sicknotes" />
                    </p>
                    <a class="btn sicknote-button" href="${formUrlPrefix}/sicknote/new">
                        <i class="icon-plus"></i>&nbsp;<spring:message code="sicknotes.new" />
                    </a>
                    <a class="btn btn-right" href="#" media="print" onclick="window.print(); return false;">
                        <i class="icon-print"></i>&nbsp;<spring:message code='Print' />
                    </a>
                </legend>

            </div>

        </div>

        <div class="grid_12">

            <div class="second-legend">
                <p style="float:left">
                    <spring:message code="sicknotes.statistics" />&nbsp;${statistics.year}
                </p>
                <p style="float:right">
                    <spring:message code="Effective"/>&nbsp;<joda:format style="M-" value="${statistics.created}"/>
                </p>
            </div>

        </div>
        
        <div class="grid_12">
            &nbsp;
        </div>

        <div class="grid_12">
            
            <table class="table">
                <tr>
                    <td>
                        <spring:message code="sicknotes.number" />
                    </td>
                    <td>
                        <c:out value="${statistics.totalNumberOfSickNotes}" /> 
                    </td>
                </tr>
                <tr>
                    <td>
                        <spring:message code="sicknotes.days.number" />  
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${statistics.totalNumberOfSickDays}" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <spring:message code="sicknotes.average.duration" />
                    </td>
                    <td>
                        <fmt:formatNumber maxFractionDigits="1" value="${statistics.averageDurationOfDisease}" />
                    </td>
                </tr>
            </table>
            
        </div>
    </div>
</div>

</body>

</html>
