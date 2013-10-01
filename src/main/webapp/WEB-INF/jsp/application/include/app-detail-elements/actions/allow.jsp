<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<button type="button" class="btn btn-success" onclick="$('#reject').hide(); $('#refer').hide();  $('#cancel').hide(); $('#confirm').show();">
    <i class="icon-check icon-white"></i>&nbsp;<spring:message code='app.state.ok' />
</button>


