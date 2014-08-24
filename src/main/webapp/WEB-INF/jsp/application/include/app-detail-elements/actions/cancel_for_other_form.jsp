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

<div id="cancel" class="confirm-red" style="display: none">

    <if test="${!empty errors}">
        <script type="text/javascript">
            $(document).ready(function() {
                showErrorDivIfAction("cancel");
            });
        </script>
    </if>
    
    <form:form class="stretched" method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
        <c:if test="${!empty errors}">
            <div id="reject-error">
                <spring:message code="error.reason" />
            </div>
        </c:if>
        <b><spring:message code='cancel.confirm' /></b>
        <br /><br />
        <spring:message code='comment' />, 
        <c:choose>
            <%-- comment is obligat if it's not the own application or if the application is in status allowed --%>
            <c:when test="${application.person.id != loggedUser.id || application.status.number == 1}">
                <spring:message code="obligat" />
            </c:when>
            <%-- otherwise comment is not obligat --%>
            <c:otherwise>
                <spring:message code="optional" />
            </c:otherwise>
        </c:choose>
        : (<span id="text-cancel"></span><spring:message code="max.chars" />)        
        <br />
        <form:textarea rows="1" path="reason" onkeyup="count(this.value, 'text-cancel');" onkeydown="maxChars(this,200); count(this.value, 'text-cancel');" />
        <br /><br />
        <button type="submit" class="btn">
            <i class="icon-remove-circle"></i>&nbsp;<spring:message code='delete' />
        </button>
        <button type="button" class="btn" onclick="$('#cancel').hide();">
            <i class="icon-remove"></i>&nbsp;<spring:message code='cancel' />
        </button>
    </form:form>
</div> 
