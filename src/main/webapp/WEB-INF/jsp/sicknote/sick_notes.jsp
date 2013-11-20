
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
    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var regional = "${pageContext.request.locale.language}";

            createDatepickerInstanceForSickNote(regional);

        });
        
        function validate() {

            $("span#from-error").empty();
            $("span#from-to").empty(); 
            
            var from = $("input#from").val();
            var to = $("input#to").val();
            
            var error = 0;
            
            if(from == "") {
               $("span#from-error").html("darf nicht leer sein");
               error = 1;
            } else {
              if(!isValidDate(from)) {
                  $("span#from-error").html("ungültige Eingabe");
                  error = 1;  
              }  
            } 
            
            if(to == "") {
                $("span#to-error").html("darf nicht leer sein"); 
                error = 1;
            } else {
                if(!isValidDate(to)) {
                    $("span#to-error").html("ungültige Eingabe");
                    error = 1;
                }
            } 
            
            if(error == 0) {
                $("form#searchRequest-form").submit(); 
            }
            
        }
        
    </script>
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
                    <a href="#changeViewModal" role="button" class="btn sicknote-button" data-toggle="modal">
                        <i class="icon-eye-open"></i>&nbsp;<spring:message code="filter" />
                    </a>
                </legend>

            </div>

            <div id="changeViewModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h3 id="myModalLabel"><spring:message code="filter" /></h3>
                </div>
                <form:form method="POST" id="searchRequest-form" action="${formUrlPrefix}/sicknote/filter" modelAttribute="searchRequest" class="form-horizontal">
                <div class="modal-body">

                    <div class="control-group">
                        <label class="control-label" for="employee"><spring:message code="staff" /></label>

                        <div class="controls">
                            <form:select path="personId" id="employee" cssErrorClass="error">
                                <form:option value="-1"><spring:message code="staff.all" /></form:option>
                                <c:forEach items="${persons}" var="person">
                                    <form:option value="${person.id}">${person.firstName}&nbsp;${person.lastName}</form:option>
                                </c:forEach>
                            </form:select>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="from"><spring:message code="From" /></label>

                        <div class="controls">
                            <form:input path="from" id="from" cssClass="input-medium" />
                            <span class="help-inline error" id="from-error"></span>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label" for="to"><spring:message code="To" /></label>

                        <div class="controls">
                            <form:input path="to" id="to" cssClass="input-medium" />
                            <span class="help-inline error" id="to-error"></span>
                        </div>
                    </div>
                    
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" type="button" onclick="validate();"><spring:message code="go" /></button>
                    <button class="btn" data-dismiss="modal" aria-hidden="true"><spring:message code="cancel" /></button>
                </div>
                </form:form>
            </div>

        </div>

        <div class="grid_12">

            <div class="second-legend">
                <p style="float:left">
                    <spring:message code="time"/>:&nbsp;<joda:format style="M-" value="${from}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${to}"/>
                </p>
                <p style="float:right">
                    <spring:message code="Effective"/>&nbsp;<joda:format style="M-" value="${today}"/>
                </p>
            </div>

        </div>

        <div class="grid_12">

            <c:choose>

                <c:when test="${empty sickNotes}">
                    <spring:message code="sicknotes.none" />
                </c:when>

                <c:otherwise>
                    <table class="app-tbl centered-tbl sortable-tbl tablesorter zebra-table" cellspacing="0">
                        <thead>
                        <tr>
                            <th><spring:message code="name" /></th>
                            <th><spring:message code="time" /></th>
                            <th><spring:message code="work.days" /></th>
                            <th><spring:message code="sicknotes.aub.short" /></th>
                            <th class="print-invisible"><spring:message code="app.date.overview" /></th>
                            <th class="print-invisible"><spring:message code="table.detail" /></th>
                            <th class="print-invisible"><spring:message code="edit" /></th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${sickNotes}" var="sickNote" varStatus="loopStatus">
                            <tr>
                                <td>
                                    <c:out value="${sickNote.person.firstName}" />&nbsp;<c:out value="${sickNote.person.lastName}" />
                                </td>
                                <td>
                                    <joda:format style="M-" value="${sickNote.startDate}"/>&nbsp;-&nbsp;<joda:format style="M-" value="${sickNote.endDate}"/>
                                </td>
                                <td>
                                    <fmt:formatNumber maxFractionDigits="1" value="${sickNote.workDays}" />
                                </td>
                                <td>
                                    <c:if test="${sickNote.aubPresent}">
                                        <img src="<spring:url value='/images/black_success.png' />" /> 
                                    </c:if>
                                        <%-- TODO: other possibility is to show x if not present--%>
                                        <%--<c:choose>--%>
                                        <%--<c:when test="${sickNote.aubPresent}">--%>
                                        <%--<img src="<spring:url value='/images/black_success.png' />" /> --%>
                                        <%--</c:when>--%>
                                        <%--<c:otherwise>--%>
                                        <%--<img src="<spring:url value='/images/black_fail.png' />" />--%>
                                        <%--</c:otherwise>--%>
                                        <%--</c:choose>--%>
                                </td>
                                <td class="print-invisible">
                                    <joda:format style="M-" value="${sickNote.lastEdited}"/> 
                                </td>
                                <td class="print-invisible">
                                    <a href="${formUrlPrefix}/sicknote/${sickNote.id}">
                                        <img src="<spring:url value='/images/playlist.png' />" />
                                    </a>
                                </td>
                                <td class="print-invisible">
                                    <a href="${formUrlPrefix}/sicknote/${sickNote.id}/edit"><img src="<spring:url value='/images/edit.png' />" /></a>
                                </td>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>

            </c:choose>

        </div>
    </div>
</div>

</body>

</html>
