<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

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
        <button type="button" class="btn btn-default pull-right" onclick="this.form.submit();">
            <i class="icon-bell"></i>&nbsp;<spring:message code='remind.chef' />
        </button>
    </form:form>
</c:if>
        
        