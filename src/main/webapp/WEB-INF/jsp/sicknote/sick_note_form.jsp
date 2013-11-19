<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>

    <script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

    <script type="text/javascript">
        $(document).ready(function() {

            var regional = "${pageContext.request.locale.language}";

            $.datepicker.setDefaults($.datepicker.regional[regional]);
            
            $("#from, #to").datepicker({
                numberOfMonths: 1,
                onSelect: function(date) {
                    if (this.id == "from") {
                        $("#to").datepicker("setDate", date);
                    } 
                },
                beforeShowDay: function (date) {

                    // if day is saturday or sunday, highlight it
                    if (date.getDay() == 6 || date.getDay() == 0) {
                        return [true, "notworkday"];
                    } else {
                        return [true, ""];
                    } 

                }
            });
            
        });
    </script>
    
</head>
<body>

<spring:url var="formUrlPrefix" value="/web"/>

<%@include file="../include/menu_header.jsp" %>

<div id="content">

    <div class="container_12">

        <div class="grid_12">

            <div class="overview-header">
                <legend>
                    <p><spring:message code="sicknotes.new" /></p>
                </legend>
            </div>
            
            <form:form method="POST" action="#" modelAttribute="sickNote" class="form-horizontal form-sicknote">

                <div class="control-group">
                    <label class="control-label" for="employee"><spring:message code='staff'/></label>

                    <div class="controls">
                        <form:select path="person" id="employee" cssErrorClass="error">
                            <c:forEach items="${persons}" var="person">
                                <form:option value="${person.id}">${person.firstName}&nbsp;${person.lastName}</form:option>
                            </c:forEach>
                        </form:select>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="from"><spring:message code='From' /></label>

                    <div class="controls">
                        <form:input id="from" path="startDate" cssClass="input-medium" />
                        <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="to"><spring:message code='To' /></label>

                    <div class="controls">
                        <form:input id="to" path="endDate" cssClass="input-medium" />
                        <span class="help-inline"><form:errors path="endDate" cssClass="error"/></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label"><spring:message code='sicknotes.aub'/></label>
                    <div class="controls">
                        <form:radiobutton path="aubPresent" value="true" />&nbsp;<spring:message code='yes' />&nbsp;&nbsp;
                        <form:radiobutton path="aubPresent" value="false" />&nbsp;<spring:message code='no' />
                        <span class="help-inline"><form:errors path="aubPresent" cssClass="error"/></span>
                    </div>
                </div>
                
                <div class="control-group">
                    <label class="control-label" for="comment">
                        <spring:message code='comment'/>&nbsp;<spring:message code='app.optional' />
                    </label>
                    <div class="controls">
                        <span id="text-comment"></span><spring:message code="max.chars" />
                        <br />
                        <form:textarea id="comment" path="comment" rows="5" cssErrorClass="error" onkeyup="count(this.value, 'text-comment');" onkeydown="maxChars(this,200); count(this.value, 'text-comment');" />
                        <span class="help-inline"><form:errors path="comment" cssClass="error"/></span>
                    </div>
                </div>

                <hr/>
                
                <div class="control-group">
                    <button class="btn" type="submit"><i class='icon-ok'></i>&nbsp;<spring:message code="save" /></button>
                    <a class="btn" href="#"><i class='icon-remove'></i>&nbsp;<spring:message code='cancel'/></a>
                </div>
            
            </form:form>
            
        </div>
        
    </div>
    
</div>    

</body>
</html>