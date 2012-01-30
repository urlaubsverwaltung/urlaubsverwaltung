<%-- 
    Document   : app_actions
    Created on : 30.01.2012, 16:31:03
    Author     : Aljona Murygina
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>



                    <%-- if user wants to cancel an application --%>
                    <c:if test="${stateNumber == 4}">
                        <form:form method="put" action="${formUrlPrefix}/application/${application.id}/cancel">
                            <spring:message code='cancel.confirm' /> <br /><br />
                            <input type="submit" class="button confirm" name="<spring:message code='delete' />" value="<spring:message code='delete' />" />
                            <a class="button back" href="${formUrlPrefix}/overview"><spring:message code='cancel' /></a>
                        </form:form>
                    </c:if>

                    <%-- application is waiting --%>            
                    <c:if test="${stateNumber == 0}">

                        <sec:authorize access="hasRole('role.boss')">         

                            <form:form method="put" action="${formUrlPrefix}/application/${application.id}/allow"> 
                                <input class="confirm" type="submit" name="<spring:message code='app.state.ok' />" value="<spring:message code='app.state.ok' />" class="button" />    
                                <input class="back" type="button" name="<spring:message code='app.state.no' />" value="<spring:message code='app.state.no' />" onclick="$('#reject').show(1000);" />
                            </form:form>   
                                <form:form method="put" action="${formUrlPrefix}/application/${application.id}/reject" modelAttribute="comment">
                                    <br /><form:errors path="*" cssClass="error" />
                            <br />
                            <br /> 

                            <div id="reject" style="display: none">    
                                    <spring:message code='reason' />&nbsp;<form:input path="text" cssErrorClass="error" />   
                                    <input type="submit" name="<spring:message code='ok' />" value="<spring:message code='ok' />" class="button" />
                                </form:form>
                            </div>    

                        </sec:authorize>

                    </c:if>

                    <%-- application is allowed --%>  
                    <c:if test="${stateNumber == 1}">
                        
                        <a class="button print" href="${formUrlPrefix}/application/${app.id}/print"><spring:message code='app' />&nbsp;<spring:message code='print' /></a>

                        <sec:authorize access="hasRole('role.office')">

                            <input type="button" onclick="$('#sick').show();" name="<spring:message code='add.sickdays' />" value="<spring:message code='add.sickdays' />" />
                            <br />
                            <br />
                            <br />

                            <form:form method="put" action="${formUrlPrefix}/application/${application.id}/sick" modelAttribute="appForm">

                                <div id="sick" style="display: none">
                                    <spring:message code='staff.sick' />
                                    <br />
                                    <br />
                                    <form:input path="sickDays" />   
                                    <input type="submit" name="<spring:message code='save' />" value="<spring:message code='save' />" class="button" />
                                </div>
                                <br />
                                <br />
                                <form:errors path="*" cssClass="error" />
                            </form:form>

                        </sec:authorize>

                    </c:if>
                    
                    <c:if test="${stateNumber == 2}">
                        <a class="button print" href="${formUrlPrefix}/application/${app.id}/print"><spring:message code='app' />&nbsp;<spring:message code='print' /></a>
                    </c:if>
