<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<button type="button" class="btn btn-right btn-danger" onclick="$('#reject').hide(); $('#confirm').hide(); $('#refer').hide(); $('#cancel').show();">
    <i class="icon-trash icon-white"></i>&nbsp;<spring:message code='app.state.cancel' />
</button> 

 
