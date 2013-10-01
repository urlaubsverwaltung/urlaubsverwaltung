<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<button type="button" class="btn btn-primary" onclick="$('#reject').hide(); $('#confirm').hide(); $('#cancel').hide(); $('#refer').show();">
    <i class="icon-share-alt icon-white"></i>&nbsp;<spring:message code='app.state.refer' />
</button>
