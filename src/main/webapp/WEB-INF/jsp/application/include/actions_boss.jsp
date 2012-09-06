<%-- 
    Document   : actions_boss
    Created on : 06.09.2012, 11:56:55
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<%-- Boss is able to:
* print the application for leave in every state
* refer the application for leave to another boss
* allow the application for leave
* reject the application for leave
--%>

<%-- Print the application for leave --%>
<%@include file="./actions/print.jsp" %>

<c:if test="${application.status.number == 0}">    

    <%-- BUTTONS --%>

    <%-- Refer the application for leave --%>
    <input class="btn btn-primary" type="button" name="<spring:message code='app.state.refer' />" 
           value="<spring:message code='app.state.refer' />" 
           onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();" /> 

    <%-- Allow the application for leave --%>
    <input class="btn btn-success" type="button" name="<spring:message code='app.state.ok' />" 
           value="<spring:message code='app.state.ok' />" 
           onclick="$('#reject').hide(); $('#refer').hide();  $('#cancel').hide(); $('#confirm').show();" />    

    <%-- Reject the application for leave --%>
    <input class="btn btn-danger" type="button" name="<spring:message code='app.state.no' />" 
           value="<spring:message code='app.state.no' />" 
           onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();" />


    <%-- FORMS --%>

    <%-- Refer the application for leave --%>
    <form:form method="put" action="${formUrlPrefix}/application/${application.id}/refer" modelAttribute="modelPerson">
        <div id="refer" style="display: none" class="confirm-green">
            <b><spring:message code="please.refer" /></b>
            <br /><br />
            <form:select path="loginName">
                <c:forEach items="${vips}" var="p">
                    <option value="${p.loginName}"><c:out value="${p.firstName} ${p.lastName}" /></option>
                </c:forEach>
            </form:select>
            &nbsp;
            <input type="submit" class="btn" name="<spring:message code='ok' />" value="<spring:message code='ok' />" />
            <input type="button" class="btn" name="<spring:message code='cancel' />" value="<spring:message code='cancel' />" onclick="$('#refer').hide();" />
        </div>
    </form:form> 

    <%-- Allow the application for leave --%>
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

    <%-- Reject the application for leave --%>
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

</c:if>  
