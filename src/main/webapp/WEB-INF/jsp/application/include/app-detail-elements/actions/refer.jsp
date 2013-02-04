<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<input class="btn btn-primary" type="button" name="<spring:message code='app.state.refer' />" 
       value="<spring:message code='app.state.refer' />" 
       onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();" /> 

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/refer" modelAttribute="modelPerson">
    <div id="refer" style="display: none" class="confirm-green">
        <b><spring:message code="please.refer" /></b>
        <br /><br />
        <form:select path="loginName">
            <c:forEach items="${vips}" var="p">
                <option value="${p.loginName}"><c:out value="${p.firstName} ${p.lastName}" /></option>
            </c:forEach>
        </form:select>
        &nbsp;
        <input type="submit" class="btn" name="<spring:message code='ok' />" value="<spring:message code='ok' />" />
        <input type="button" class="btn" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#refer').hide();" />
    </div>
</form:form>
