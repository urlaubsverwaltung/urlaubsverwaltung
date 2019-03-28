<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(document).ready(function () {

        // re-calculate vacation days when changing the day length

        var urlPrefix = '<spring:url value="/api" />';
        var personId = '<c:out value="${person.id}" />';

        $('input[name="dayLength"]').on('change', function () {

            var dayLength = this.value;
            var startDate = $('input#from').datepicker("getDate");
            var toDate = $('input#to').datepicker("getDate");

            sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");
            sendGetDepartmentVacationsRequest(urlPrefix, startDate, toDate, personId, "#departmentVacations");

        });

    });

</script>
