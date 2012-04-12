<%-- 
    Document   : actions
    Created on : 12.04.2012, 21:11:00
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<a class="button print" href="${formUrlPrefix}/application/${application.id}/print"><spring:message code='app' />&nbsp;<spring:message code='print' /></a>
<sec:authorize access="hasRole('role.office')">
    <a class="button staff" href="${formUrlPrefix}/staff/${application.person.id}/overview" /><spring:message code="staff.back" /></a>
</sec:authorize>
    <sec:authorize access="hasRole('role.user')">
<!--        TO DO  -->
        <a class="button remind" href="${formUrlPrefix}/application/${application.id}/remind"><spring:message code='remind.chef' /></a>
<!--        Notiz of Office-->
    </sec:authorize>
<br /><br />


<%-- application is waiting --%>            
<c:if test="${application.status.number == 0}">

    <sec:authorize access="hasRole('role.boss')">         

        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow"> 
            <input class="confirm" type="submit" name="<spring:message code='app.state.ok' />" value="<spring:message code='app.state.ok' />" class="button" />    
            <input class="back" type="button" name="<spring:message code='app.state.no' />" value="<spring:message code='app.state.no' />" onclick="$('#reject').show();" />
            <input class="refer" type="button" name="<spring:message code='app.state.refer' />" value="<spring:message code='app.state.refer' />" onclick="$('#refer').show();" />
        </form:form>   
        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
            <br />
            <div id="reject" style="
                 <c:choose>
                     <c:when test="${empty errors}">display: none</c:when>
                     <c:otherwise>display: block</c:otherwise>
                 </c:choose>
                 ">            
                <spring:message code='reason' />: (<spring:message code="max.chars" />)
                <br />
                <form:textarea path="reason" cssErrorClass="error" cssClass="reject-text" />
                <br />
                <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />
                <script type="text/javascript">
                    $(document).ready(function() {
                        $('#reject-error').show('drop', 500);
                    });
                </script>
                <form:errors path="reason" cssClass="error" id="reject-error" />
            </div>
        </form:form>
        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/refer" modelAttribute="modelPerson">
            <div id="refer" style="display: none">
                <spring:message code="please.refer" />
                <form:select path="loginName">
                    <c:forEach items="${vips}" var="p">
                        <option value="${p.loginName}"><c:out value="${p.firstName} ${p.lastName}" /></option>
                    </c:forEach>
                </form:select>
                <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />
            </div>
        </form:form>
    </sec:authorize>

</c:if>


<%-- if application has status rejected --%>
<c:if test="${application.status.number == 2}">
        <div id="rejected-app">
            <b><spring:message code="app.reject.reason" arguments="${rejectDate}, ${comment.nameOfCommentingPerson}" /></b><br /><br />
        <c:out value="${comment.reason}" />
        </div>
</c:if>


<%-- if user wants to cancel an application --%>
<c:if test="${stateNumber == 4}">
    <div id="cancel">
        <sec:authorize access="hasRole('role.office')">
            <c:if test="${application.status.number == 0 || application.status.number == 1}">      
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                    <spring:message code='cancel.confirm' /><br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                </form:form>
            </c:if>
        </sec:authorize>

        <sec:authorize access="hasRole('role.user')">
            <c:if test="${application.status.number == 0}">      
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                    <spring:message code='cancel.confirm' /><br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                </form:form>
            </c:if>
        </sec:authorize>  

        <sec:authorize access="hasRole('role.boss')">
            <c:if test="${application.status.number == 0}">      
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                    <spring:message code='cancel.confirm' /><br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                </form:form>
            </c:if>
        </sec:authorize>  
        </div>

</c:if>
