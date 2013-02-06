<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<input class="btn btn-danger" type="button" name="<spring:message code='app.state.no' />" 
       value="<spring:message code='app.state.no' />" 
       onclick="$('#refer').hide(); $('#confirm').hide();  $('#cancel').hide(); $('#reject').show();" />


