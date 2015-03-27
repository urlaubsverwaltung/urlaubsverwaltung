<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script type="text/javascript">
    $(document).ready(function () {

        var dayLength = $('input[name="howLong"]:checked').val();

        var $fullDay = $('.full-day');
        var $halfDay = $('.half-day');

        if (dayLength === 'FULL') {
            $fullDay.show();
            $halfDay.hide();
        }

        if (dayLength === 'MORNING') {
            $halfDay.show();
            $fullDay.hide();
        }

        if (dayLength === 'NOON') {
            $halfDay.show();
            $fullDay.hide();
        }


        // re-calculate vacation days when changing the day length

        var urlPrefix = '<spring:url value="/api" />';
        var personId = '<c:out value="${person.id}" />';

        $('input[name="howLong"]').on('change', function () {

            var dayLength = this.value;
            var startDate;
            var toDate;

            if (dayLength === 'FULL') {
                startDate = $('input#from').datepicker("getDate");
                toDate = $('input#to').datepicker("getDate");
            } else {
                var atDate = $('input#at').datepicker("getDate");
                startDate = atDate;
                toDate = atDate;
            }

            sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, personId, ".days");

        });

    });

</script>