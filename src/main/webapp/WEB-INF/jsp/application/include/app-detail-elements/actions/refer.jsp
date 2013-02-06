<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<input class="btn btn-primary" type="button" name="<spring:message code='app.state.refer' />" 
       value="<spring:message code='app.state.refer' />" 
       onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();" /> 

