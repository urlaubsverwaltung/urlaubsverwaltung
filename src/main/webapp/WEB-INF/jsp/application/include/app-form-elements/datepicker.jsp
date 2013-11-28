<%-- 
    Document   : datepicker
    Created on : 06.09.2012, 17:15:21
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

<script type="text/javascript">
    $(document).ready(function() {
        
        var datepickerLocale = "${pageContext.request.locale.language}";
        var urlPrefix = "<spring:url value='/web/calendar/' />";
        var vacationUrl = "<spring:url value='/web/calendar/vacation' />";

        var personId = '<c:out value="${person.id}" />';
        
        createDatepickerInstances(datepickerLocale, urlPrefix, vacationUrl, personId);
        
    });
</script>