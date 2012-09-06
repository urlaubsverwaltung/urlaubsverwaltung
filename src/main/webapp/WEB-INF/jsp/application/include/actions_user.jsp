<%-- 
    Document   : actions_user
    Created on : 06.09.2012, 11:56:47
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<%-- User is able to:
* print the application for leave in every state
* remind the boss if the application for leave is in state waiting
* cancel the application for leave if it is in state waiting or allowed
--%>

<%-- Print the application for leave --%>
<%@include file="./actions/print.jsp" %>

<%-- Remind the boss --%>
<c:if test="${application.status.number == 0}"> <%-- only for applications that has state waiting --%>
    <form:form method="put" action="${formUrlPrefix}/application/${application.id}/remind" style="display:inline;">
        <c:if test="${noWay == true}">
            <script type="text/javascript">
                $(document).ready(function() {
                    alert('<spring:message code='dont.get.impatient' />');
                });
            </script>
        </c:if>
        <c:if test="${isSent == true}">
            <script type="text/javascript">
                $(document).ready(function() {
                    alert('<spring:message code='chef.is.reminded' />');
                });
            </script>
        </c:if>
        <c:if test="${alreadySent == true}">
            <script type="text/javascript">
                $(document).ready(function() {
                    alert('<spring:message code='already.sent.today' />');
                });
            </script>
        </c:if>
        <input type="button" class="btn btn-primary" onclick="this.form.submit();" name="<spring:message code='remind.chef' />" value="<spring:message code='remind.chef' />" />
    </form:form>
</c:if>

<%-- Cancel the application for leave --%> 
<%@include file="./actions/cancel.jsp" %>