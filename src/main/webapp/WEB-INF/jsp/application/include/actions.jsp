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
    <%-- cancel application button for office --%>
    <%-- show only if application is not cancelled yet --%>
    <c:if test="${application.status.number != 3}">
        <input type="button" class="cancel" name="<spring:message code='app.state.cancel' />" value="<spring:message code='app.state.cancel' />" onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();" />
    </c:if>
</sec:authorize>
<sec:authorize access="hasRole('role.user')">
    <c:if test="${application.status.number == 0}">
        <a class="button remind" href="${formUrlPrefix}/application/${application.id}/remind"><spring:message code='remind.chef' /></a>
    </c:if>
</sec:authorize>
    <c:if test="${(loggedUser.role.number == 0 || loggedUser.role.number == 1) && application.status.number != 3 && application.person.id == loggedUser.id}">
        <input type="button" class="cancel" name="<spring:message code='app.state.cancel' />" value="<spring:message code='app.state.cancel' />" onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();" />
    </c:if>
<br /><br />


<%-- application is waiting --%>            
<c:if test="${application.status.number == 0}">

    <sec:authorize access="hasRole('role.boss')">         

        <!-- Action-Buttons -->
        <input class="refer" type="button" name="<spring:message code='app.state.refer' />" value="<spring:message code='app.state.refer' />" onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();" />
        <c:if test="${application.person.id != loggedUser.id}">
            <input class="confirm" type="button" name="<spring:message code='app.state.ok' />" value="<spring:message code='app.state.ok' />" onclick="$('#reject').hide(); $('#refer').hide();  $('#cancel').hide(); $('#confirm').show();" />    
            <input class="back" type="button" name="<spring:message code='app.state.no' />" value="<spring:message code='app.state.no' />" onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();" />
        </c:if>

        <!-- Allow an application-->
        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow" modelAttribute="comment">
            <div id="confirm" style="display:none">
                <spring:message code='comment' />, <spring:message code="optional" />: (<spring:message code="max.chars" />)
                <br />
                <form:textarea path="reason" cssClass="text" onkeydown="maxChars(this,200);" />
                <br />
                <input type="submit" name="<spring:message code='app.state.ok.short' />" value="<spring:message code='app.state.ok.short' />" class="button" />
                <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#confirm').hide();" />
            </div>
        </form:form>

        <!-- Reject an application-->
        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
            <br />
            <div id="reject" style="
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
                <spring:message code='reason' />, <spring:message code="obligat" />: (<spring:message code="max.chars" />)
                <br />
                <form:textarea path="reason" cssErrorClass="error" cssClass="text" onkeydown="maxChars(this,200);" />
                <br />
                <input type="submit" name="<spring:message code='app.state.no.short' />" value="<spring:message code='app.state.no.short' />" class="button" />
                <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#reject').hide();" />
            </div>
        </form:form>

        <!-- Refer an application-->
        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/refer" modelAttribute="modelPerson">
            <div id="refer" style="display: none">
                <spring:message code="please.refer" />
                <form:select path="loginName">
                    <c:forEach items="${vips}" var="p">
                        <option value="${p.loginName}"><c:out value="${p.firstName} ${p.lastName}" /></option>
                    </c:forEach>
                </form:select>
                <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />
                <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#refer').hide();" />
            </div>
        </form:form>           
    </sec:authorize>

</c:if>


<%-- if user wants to cancel an application --%>
<c:if test="${stateNumber == 4}">
    <script type="text/javascript">
        $(document).ready(function() {
            $('#cancel').show();
        });
     </script>
</c:if>
    <div id="cancel" style="display: none">
        <sec:authorize access="hasRole('role.office')">
            <c:if test="${application.status.number == 0 || application.status.number == 1}">
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
                    <spring:message code='cancel.confirm' />
                    <br /><br />
                    <spring:message code='comment' />, <spring:message code="optional" />: (<spring:message code="max.chars" />)
                    <br />
                    <form:textarea path="reason" cssClass="text" onkeydown="maxChars(this,200);" />
                    <br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#cancel').hide();" />
                </form:form>
            </c:if>
        </sec:authorize>

        <sec:authorize access="hasRole('role.user')">
            <c:if test="${application.status.number == 0 || application.status.number == 1}">
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
                    <spring:message code='cancel.confirm' />
                    <br /><br />
                    <c:if test="${!empty errors}">
                    <div id="reject-error">
                    </div>
                </c:if>
                    <spring:message code='app.comment' />, <spring:message code="optional" />: (<spring:message code="max.chars" />)
                    <br />
                    <form:textarea path="reason" cssClass="text" onkeydown="maxChars(this,200);" />
                    <br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#cancel').hide();" />
                </form:form>
            </c:if>
        </sec:authorize>  

        <sec:authorize access="hasRole('role.boss')">
            <c:if test="${application.status.number == 0 || application.status.number == 1}">
                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel" modelAttribute="comment">
                    <spring:message code='cancel.confirm' />
                    <br /><br />
                    <c:if test="${!empty errors}">
                    <div id="reject-error">
                    </div>
                </c:if>
                    <spring:message code='app.comment' />, <spring:message code="optional" />: (<spring:message code="max.chars" />)
                    <br />
                    <form:textarea path="reason" cssClass="text" onkeydown="maxChars(this,200);" />
                    <br /><br />
                    <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                    <input type="button" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#cancel').hide();" />
                </form:form>
            </c:if>
        </sec:authorize>  
    </div>

