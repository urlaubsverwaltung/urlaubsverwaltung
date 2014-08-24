<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<form:form class="stretched" method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
    <div id="reject" class="confirm-red" style="display: none">

        <if test="${!empty errors}">
            <script type="text/javascript">
                $(document).ready(function() {
                    showErrorDivIfAction("reject");
                });
            </script> 
        </if>
        
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
        <form:textarea rows="1" path="reason" cssErrorClass="error" onkeyup="count(this.value, 'text-reject');" onkeydown="maxChars(this,200); count(this.value, 'text-reject');" />
        <br />

            <button type="submit" class="btn">
                <i class="icon-ban-circle"></i>&nbsp;<spring:message code='app.state.no.short' />
            </button>
            <button type="button" class="btn" onclick="$('#reject').hide();">
                <i class="icon-remove"></i>&nbsp;<spring:message code='cancel' />
            </button>    
            
    </div>
</form:form>
