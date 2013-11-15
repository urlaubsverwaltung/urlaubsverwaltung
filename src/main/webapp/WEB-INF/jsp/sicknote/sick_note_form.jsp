<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html>
<head>
    <title><spring:message code="title"/></title>
    <%@include file="../include/header.jsp" %>
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
            
            <form:form method="POST" action="#" modelAttribute="sickNote" class="form-horizontal">

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
                    <label class="control-label"><spring:message code='time'/></label>
                    <div class="controls">
                        <spring:message code='from' />&nbsp;<form:input path="startDate" />
                        <spring:message code='to' />&nbsp;<form:input path="endDate" />
                        <span class="help-inline"><form:errors path="startDate" cssClass="error"/></span>
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
                    <label class="control-label" for="comment"><spring:message code='comment'/></label>
                    <div class="controls">
                        <form:textarea id="comment" path="comment" cssErrorClass="error"/>
                        <span class="help-inline"><form:errors path="comment" cssClass="error"/></span>
                    </div>
                </div>
            
            </form:form>
            
        </div>
        
    </div>
    
</div>    

</body>
</html>