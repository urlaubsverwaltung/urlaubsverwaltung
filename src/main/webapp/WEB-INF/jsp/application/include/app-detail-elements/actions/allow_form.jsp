<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow" modelAttribute="comment">
    <div id="confirm" style="display:none" class="confirm-green">
        <b><spring:message code='allow.confirm' /></b>
        <br /><br />
        <spring:message code='comment' />, <spring:message code="optional" />: (<span id="text-confirm"></span><spring:message code="max.chars" />)
        <br />
        <form:textarea path="reason" cssClass="form-textarea" onkeyup="count(this.value, 'text-confirm');" onkeydown="maxChars(this,200); count(this.value, 'text-confirm');" />
        <br />
        <input type="submit" class="btn" name="<spring:message code='app.state.ok.short' />" value="<spring:message code='app.state.ok.short' />" />
        <input type="button" class="btn" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#confirm').hide();" />
    </div>
</form:form>
