<%-- 
    Document   : day-length-selector
    Created on : 06.09.2012, 17:17:29
    Author     : Aljona Murygina - murygina@synyx.de
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<style type="text/css">

    .half-day {
        display: none;
    }

</style>

<c:if test="${appForm.howLong != null}">
    <script type="text/javascript">
        $(document).ready(function() {
                    
            var dayLength = "<c:out value='${appForm.howLong}' />";
            
            if(dayLength.indexOf("FULL") != -1) {
                $('.full-day').show(); $('.half-day').hide();
            } 
                
            if(dayLength.indexOf("MORNING") != -1) {
                $('.half-day').show(); $('.full-day').hide();
            }
                
            if(dayLength.indexOf("NOON") != -1) {
                $('.half-day').show(); $('.full-day').hide();
            }

        });

    </script>
</c:if>
