<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>


<input type="button" class="btn btn-danger" name="<spring:message code='app.state.cancel' />" value="<spring:message code='app.state.cancel' />" onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();" />


