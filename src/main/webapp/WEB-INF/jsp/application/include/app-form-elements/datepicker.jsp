<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<script src="<spring:url value='/js/datepicker.js' />" type="text/javascript" ></script>

<script type="text/javascript">
    $(document).ready(function() {

        var datepickerLocale = "${pageContext.response.locale.language}";
        var urlPrefix = "<spring:url value='/api' />";
        var personId = '<c:out value="${person.id}" />';

        var getPersonId = function() {
            return personId;
        };

        var onSelect = function(selectedDate) {

            instance = $(this).data("datepicker"),
                    date = $.datepicker.parseDate(
                            instance.settings.dateFormat ||
                                    $.datepicker._defaults.dateFormat,
                            selectedDate, instance.settings);


            var $from = $("#from");
            var $to = $("#to");
            var $at = $("#at");

            if (this.id === "from" && $to.val() === "") {
                $to.datepicker("setDate", selectedDate);
            }

            var dayLength = $('input:radio[name=howLong]:checked').val();
            var startDate = "";
            var toDate = "";

            if (dayLength === "FULL") {
                startDate = $from.datepicker("getDate");
                toDate = $to.datepicker("getDate");
            } else {
                startDate = $at.datepicker("getDate");
                toDate = $at.datepicker("getDate");
            }

            sendGetDaysRequest(urlPrefix, startDate, toDate, dayLength, getPersonId(), ".days");

        };

        var selectors = ["#from", "#to", "#at"];

        createDatepickerInstances(selectors, datepickerLocale, urlPrefix, getPersonId, onSelect);
        
    });
</script>