<%-- 
    Document   : cancel
    Created on : 06.09.2012, 13:44:58
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<c:if test="${stateNumber == 4}">
    <script type="text/javascript">
        $(document).ready(function() {
            $('#cancel').show();
        });
    </script>
</c:if>

<div id="cancel" class="confirm-red" style="
     <c:choose>
         <c:when test="${empty errors}">display: none</c:when>
         <c:otherwise>display: block</c:otherwise>
     </c:choose>
     ">
    <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
        <c:if test="${!empty errors}">
            <div id="reject-error">
                <spring:message code="error.reason" />
            </div>
        </c:if>
        <b><spring:message code='cancel.confirm' /></b>
        <br /><br />
        <spring:message code='comment' />, <spring:message code="optional" /> 
        : (<span id="text-cancel"></span><spring:message code="max.chars" />)        
        <br />
        <form:textarea path="reason" cssClass="form-textarea" onkeyup="count(this.value, 'text-cancel');" onkeydown="maxChars(this,200); count(this.value, 'text-cancel');" />
        <br /><br />
        <input type="submit" class="btn" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
        <input type="button" class="btn" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#cancel').hide();" />
    </form:form>
</div> 
