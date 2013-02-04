<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<input class="btn btn-danger" type="button" name="<spring:message code='app.state.no' />" 
       value="<spring:message code='app.state.no' />" 
       onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();" />


<form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
    <div id="reject" class="confirm-red" style="
         <c:choose>
             <c:when test="${empty errors}">display: none</c:when>
             <c:otherwise>display: block</c:otherwise>
         </c:choose>
         ">
        <%-- because of black magic or for other weird reasons this doesn't work
        <form:errors path="reason" cssClass="error" />
        <form:errors path="*" cssClass="error" />
        --%>
        <%-- so this is the alternative --%>
        <c:if test="${!empty errors}">
            <div id="reject-error">
                <spring:message code="error.reason" />
            </div>
        </c:if>
        <b><spring:message code='reject.confirm' /></b>
        <br /><br />
        <spring:message code='reason' />, <spring:message code="obligat" />: (<span id="text-reject"></span><spring:message code="max.chars" />)
        <br />
        <form:textarea path="reason" cssErrorClass="error" cssClass="form-textarea" onkeyup="count(this.value, 'text-reject');" onkeydown="maxChars(this,200); count(this.value, 'text-reject');" />
        <br />
        <input type="submit" class="btn" name="<spring:message code='app.state.no.short' />" value="<spring:message code='app.state.no.short' />" />
        <input type="button" class="btn" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#reject').hide();" />
    </div>
</form:form>
