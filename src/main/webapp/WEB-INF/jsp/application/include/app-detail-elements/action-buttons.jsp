<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<spring:url var="URL_PREFIX" value="/web"/>

<sec:authorize access="hasRole('USER')">
    <uv:print/>
</sec:authorize>

<c:if test="${application.status == 'WAITING' || application.status == 'ALLOWED'}">
    <div class="btn-group pull-right">
        <a class="btn btn-default dropdown-toggle" data-toggle="dropdown" href="#">
            <i class="fa fa-edit"></i>
            <span class="hidden-xs">
               <spring:message code="action"/>
            </span>
            <span class="caret"></span>
        </a>
        <ul class="dropdown-menu">
            <c:if test="${application.status == 'WAITING'}">
                <sec:authorize access="hasRole('USER')">
                    <c:if test="${application.person.id == loggedUser.id && application.status == 'WAITING'}">
                        <li>
                            <a href="#" onclick="$('form#remind').submit();">
                                <i class="fa fa-bell"></i>&nbsp;<spring:message code='remind.chef'/>
                            </a>
                        </li>
                    </c:if>
                </sec:authorize>
                <sec:authorize access="hasRole('BOSS')">
                    <li>
                        <a href="#"
                           onclick="$('#reject').hide(); $('#refer').hide(); $('#cancel').hide(); $('#confirm').show();">
                            <i class="fa fa-check"></i>&nbsp;<spring:message code='app.state.ok.short'/>
                        </a>
                    </li>
                    <li>
                        <a href="#"
                           onclick="$('#refer').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#reject').show();">
                            <i class="fa fa-ban"></i>&nbsp;<spring:message code='app.state.no.short'/>
                        </a>
                    </li>
                    <li>
                        <a href="#"
                           onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();">
                            <i class="fa fa-mail-forward"></i>&nbsp;<spring:message code='app.state.refer.short'/>
                        </a>
                    </li>
                </sec:authorize>
                <sec:authorize access="hasRole('USER')">
                    <li>
                        <a href="#"
                           onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();">
                            <i class="fa fa-trash"></i>&nbsp;<spring:message code='app.state.cancel'/>
                        </a>
                    </li>
                </sec:authorize>
            </c:if>
            <c:if test="${application.status == 'ALLOWED'}">
                <sec:authorize access="hasRole('OFFICE')">
                    <li>
                        <a href="#"
                           onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();">
                            <i class="fa fa-trash"></i>&nbsp;<spring:message code='app.state.cancel'/>
                        </a>
                    </li>
                </sec:authorize>
            </c:if>
        </ul>
    </div>
</c:if>