<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <uv:head />

    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var regional = "${pageContext.request.locale.language}";

            createDatepickerInstanceForSickNote(regional, "from", "to");

            createDatepickerInstanceForSickNote(regional, "aubFrom", "aubTo");
            
            <c:choose>
                <c:when test="${sickNote.aubPresent}">
                    showAUFields();
                </c:when>
                <c:otherwise>
                    hideAUFields();
                </c:otherwise>
            </c:choose>
            
        });
        
        function showAUFields() {
           $("div.AU").show(); 
        }
        
        function hideAUFields() {
            $("div.AU").hide();
            $("input#aubFrom").datepicker("setDate", null);
            $("input#aubTo").datepicker("setDate", null);
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

                <div class="header">
                    <legend>
                        <p>
                            <c:choose>
                                <c:when test="${sickNote.id == null}">
                                    <spring:message code="sicknotes.new" />
                                </c:when>
                                <c:otherwise>
                                    <spring:message code="sicknotes.edit" />
                                </c:otherwise>
                            </c:choose>
                        </p>
                    </legend>
                </div>

            </div>

        </div>

        <div class="row">

            <c:choose>
                <c:when test="${sickNote.id == null}">
                    <c:set var="METHOD" value="POST" />
                    <c:set var="ACTION" value="${formUrlPrefix}/sicknote" />
                </c:when>
                <c:otherwise>
                    <c:set var="METHOD" value="PUT" />
                    <c:set var="ACTION" value="${formUrlPrefix}/sicknote/${sickNote.id}/edit" />
                </c:otherwise>
            </c:choose>

            <form:form method="${METHOD}" action="${ACTION}" modelAttribute="sickNote" class="form-horizontal">

            <div class="col-xs-12 col-sm-8">

                <div class="form-group">
                    <label class="control-label col-sm-5" for="employee"><spring:message code='staff'/></label>

                    <div class="col-sm-4">
                        <c:choose>
                            <c:when test="${sickNote.id == null}">
                                <form:select path="person" id="employee" class="form-control" cssErrorClass="form-control error">
                                    <c:forEach items="${persons}" var="person">
                                        <form:option value="${person.id}">${person.niceName}</form:option>
                                    </c:forEach>
                                </form:select>
                                <span class="help-inline"><form:errors path="person" cssClass="error"/></span>
                            </c:when>
                            <c:otherwise>
                                <form:hidden path="id" />
                                <form:hidden path="person" value="${sickNote.person.id}" />
                                <c:out value="${sickNote.person.niceName}" />
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-sm-5" for="from"><spring:message code="sicknotes.time" /></label>

                    <div class="col-sm-4">
                        <spring:message code="From" />
                        <br />
                        <form:input id="from" path="startDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="form-group">
                    <label class="control-label col-sm-5" for="to">&nbsp;</label>

                    <div class="col-sm-4">
                        <spring:message code="To" />
                        <br />
                        <form:input id="to" path="endDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="form-group">
                    <label class="control-label col-sm-5"><spring:message code='sicknotes.aub'/></label>
                    <div class="col-sm-4 radio">
                        <label>
                            <form:radiobutton id="aubPresent" path="aubPresent" value="true" onclick="showAUFields();" />
                            <spring:message code='yes' />
                        </label>

                        <label>
                            <form:radiobutton id="aubNotPresent" path="aubPresent" value="false" onclick="hideAUFields();" />
                            <spring:message code='no' />
                        </label>

                        <span class="help-inline"><form:errors path="aubPresent" cssClass="error"/></span>
                    </div>
                </div>

                <div class="form-group AU">
                    <label class="control-label col-sm-5" for="aubFrom">
                        <spring:message code="sicknotes.aub.time" />
                        <br />
                        <spring:message code="sicknotes.aub.time.note" />
                    </label>

                    <div class="col-sm-4">
                        <spring:message code="From" />
                        <br />
                        <form:input id="aubFrom" path="aubStartDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="aubStartDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="form-group AU">
                    <label class="control-label col-sm-5" for="aubTo">&nbsp;</label>

                    <div class="col-sm-4">
                        <spring:message code="To" />
                        <br />
                        <form:input id="aubTo" path="aubEndDate" class="form-control" cssErrorClass="form-control error" />
                        <span class="help-inline"><form:errors path="aubEndDate" cssClass="error"/></span>
                    </div>
                </div>

            </div>

        </div>

        <div class="row">

            <div class="col-xs-12">
                
                <hr/>
                
                <div class="form-group">
                    <button class="btn btn-large btn-success col-xs-12 col-sm-2" type="submit"><i class='fa fa-check'></i>&nbsp;<spring:message code="save" /></button>
                    <a class="btn btn-default btn-large col-xs-12 col-sm-2" href="${formUrlPrefix}/sicknote"><i class='fa fa-remove'></i>&nbsp;<spring:message code='cancel'/></a>
                </div>

            </div>

        </div>

            </form:form>
            
    </div>
    
</div>    

</body>
</html>