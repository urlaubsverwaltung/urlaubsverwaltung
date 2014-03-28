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

<div id="content">

    <div class="container_12">

        <div class="grid_12">

            <div class="overview-header">
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
            
            <form:form method="${METHOD}" action="${ACTION}" modelAttribute="sickNote" class="form-horizontal form-sicknote">

                <div class="control-group">
                    <label class="control-label" for="employee"><spring:message code='staff'/></label>

                    <div class="controls">
                        <c:choose>
                            <c:when test="${sickNote.id == null}">
                                <form:select path="person" id="employee" cssErrorClass="error">
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

                <div class="control-group">
                    <label class="control-label" for="from"><spring:message code="sicknotes.time" /></label>

                    <div class="controls">
                        <spring:message code="From" />
                        <br />
                        <form:input id="from" path="startDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                        <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="to">&nbsp;</label>

                    <div class="controls">
                        <spring:message code="To" />
                        <br />
                        <form:input id="to" path="endDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                        <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="control-group">
                    <label class="control-label"><spring:message code='sicknotes.aub'/></label>
                    <div class="controls">
                        <form:radiobutton path="aubPresent" value="true" onclick="showAUFields();" />&nbsp;<spring:message code='yes' />&nbsp;&nbsp;
                        <form:radiobutton path="aubPresent" value="false" onclick="hideAUFields();" />&nbsp;<spring:message code='no' />
                        <span class="help-inline"><form:errors path="aubPresent" cssClass="error"/></span>
                    </div>
                </div>

                <div class="control-group AU">
                    <label class="control-label" for="aubFrom">
                        <spring:message code="sicknotes.aub.time" />
                        <br />
                        <spring:message code="sicknotes.aub.time.note" />
                    </label>

                    <div class="controls">
                        <spring:message code="From" />
                        <br />
                        <form:input id="aubFrom" path="aubStartDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                        <span class="help-inline"><form:errors path="aubStartDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="control-group AU">
                    <label class="control-label" for="aubTo">&nbsp;</label>

                    <div class="controls">
                        <spring:message code="To" />
                        <br />
                        <form:input id="aubTo" path="aubEndDate" cssClass="input-medium" cssErrorClass="error input-medium" />
                        <span class="help-inline"><form:errors path="aubEndDate" cssClass="error"/></span>
                    </div>
                </div>
                
                <hr/>
                
                <div class="control-group">
                    <button class="btn" type="submit"><i class='icon-ok'></i>&nbsp;<spring:message code="save" /></button>
                    <a class="btn" href="${formUrlPrefix}/sicknote"><i class='icon-remove'></i>&nbsp;<spring:message code='cancel'/></a>
                </div>
            
            </form:form>
            
        </div>
        
    </div>
    
</div>    

</body>
</html>