<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form class="stretched" method="put" action="${formUrlPrefix}/application/${application.id}/allow" modelAttribute="comment">
    <div id="confirm" style="display:none" class="confirm-green">
        <b><spring:message code='allow.confirm' /></b>
        <br /><br />
        <spring:message code='comment' />, <spring:message code="optional" />: (<span id="text-confirm"></span><spring:message code="max.chars" />)
        <br />
        <form:textarea rows="1" path="reason" onkeyup="count(this.value, 'text-confirm');" onkeydown="maxChars(this,200); count(this.value, 'text-confirm');" />
        <br />
        <button type="submit" class="btn">
            <i class="icon-check"></i>&nbsp;<spring:message code='app.state.ok.short' />
        </button>
        <button type="button" class="btn" onclick="$('#confirm').hide();">
            <i class="icon-remove"></i>&nbsp;<spring:message code='cancel' />
        </button>
    </div>
</form:form>
